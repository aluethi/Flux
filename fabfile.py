from __future__ import with_statement
from fabric.api import *
from fabric.contrib.files import exists
import time
import os


env.user = 'ubuntu'
env.key_filename = 'asl.pem'

MW = 'ec2-54-195-230-49.eu-west-1.compute.amazonaws.com'
MW2 = 'ec2-54-74-59-41.eu-west-1.compute.amazonaws.com'
CL = 'ec2-54-75-172-171.eu-west-1.compute.amazonaws.com'
CL2 = 'ec2-54-195-57-23.eu-west-1.compute.amazonaws.com'
DB = 'ec2-54-170-179-1.eu-west-1.compute.amazonaws.com'

''' host definitions '''
def middleware():
  env.hosts = [MW]


def middleware2():
  env.hosts = [MW2]


def client():
  env.hosts = [CL]


def client2():
  env.hosts = [CL2]


def database():
  env.hosts = [DB]


''' installation procedures '''
def reboot():
  sudo("reboot")


def _install_base():
  sudo('apt-get update')
  sudo('apt-get upgrade')


def _install_db():
  sudo('apt-get -q -y install postgresql-9.3 postgresql-contrib-9.3')


def _copy_db_config():
  put('prod/postgresql.conf', '~/')
  put('prod/pg_hba.conf','/')
  sudo('cp ~/postgresql.conf /etc/postgresql/9.3/main/')
  sudo('cp ~/pg_hba.conf /etc/postgresql/9.3/main/')


def _create_database(db_user, db_pass, db_name):
  put('var/flux.sql', '~/flux.sql')
  sudo('psql -c "CREATE USER %s WITH NOCREATEDB NOCREATEUSER ENCRYPTED PASSWORD E\'%s\'"' % (db_user, db_pass), user='postgres')
  sudo('psql -c "CREATE DATABASE %s WITH OWNER %s"' % (db_name, db_user), user='postgres')
  sudo('psql -f ~/flux.sql -h localhost -p 5432 -d %s -U %s' % (db_name, db_user), user='postgres')


def _truncate_db(db_name):
  sudo('psql -c "TRUNCATE MESSAGE, QUEUE, CLIENT;" -d "%s"' % db_name, user='postgres')


def setup_db():
  _install_base()
  _install_db()
  _copy_db_config()
  _create_database("flux", "flux", "flux")
  reboot()


def _install_java():
  _install_base()
  sudo('apt-get -q -y install openjdk-7-jdk')


def _build_locally():
  local('ant dist')


def _deploy_server():
  put('dist/flux-server.jar', '~/flux-server.jar')
  if not exists('~/var'):
    run('mkdir ~/var')
  put('prod/*', '~/var/')
  sudo('chmod 777 ~/flux-server.jar')


def _deploy_client():
  put('dist/flux-client.jar', '~/flux-client.jar')
  if not exists('~/var'):
    run('mkdir ~/var')
  put('prod/*', '~/var/')
  sudo('chmod 777 ~/flux-client.jar')


def setup_middleware():
  _install_base()
  _install_java()
  _build_locally()
  _deploy_server()


def setup_client():
  _install_base()
  _install_java()
  _build_locally()
  _deploy_client()


def update_client():
  _build_locally()
  _deploy_client()


def update_server():
  _build_locally()
  _deploy_server()


def upload_server_config(experiment_id):
  put('configs/%s-config.properties' % experiment_id, '~/var/config.properties')


''' experiment control methods '''
def start_server():
  run('nohup java -jar ~/flux-server.jar &> out & disown; sleep 1')
  #time.sleep(10)


''' client start methods '''
def _start_single_client(client_id, experiment, middleware_ip=None, middleware_port=5555, message_size=0):
  if middleware_ip is None:
    middleware_ip = MW
  run('nohup java -jar ~/flux-client.jar %s %s %d %s %d &> out & disown; sleep 1' % (client_id, middleware_ip, middleware_port, experiment, message_size))


def _start_multiple_clients(count, id_offset, experiment, middleware_ip=None, middleware_port=5555, message_size=0):
  for i in range(0, count):
    _start_single_client(id_offset + i, experiment, middleware_ip, middleware_port, message_size)


def _start_dialog_clients(client_id, middleware_ip=None, middleware_port=5555, message_size=0):
  _start_single_client(client_id * 2, "DialogClientWorkload", middleware_ip, middleware_port,  message_size)
  _start_single_client(client_id * 2 + 1, "DialogServerWorkload", middleware_ip, middleware_port, message_size)


def _start_multiple_dialog_clients(count, id_offset, middleware_ip=None, middleware_port=5555, message_size=0):
  for i in range(0, count):
    _start_dialog_clients(id_offset + i, middleware_ip, middleware_port, message_size)


def start_client_mixture(f1, f2, f3, middleware_ip=None, middleware_port=5555, message_size=0):
  frac1 = int(f1)
  frac2 = int(f2)
  frac3 = int(f3)
  message_size = int(message_size)
  _start_multiple_clients(frac1, 1, "OneWayClientWorkload", middleware_ip, middleware_port, message_size)
  _start_multiple_clients(frac2/2, frac1 + 100, "PoolClientWorkload", middleware_ip, middleware_port, message_size)
  _start_multiple_clients(frac2/2, frac1 + 100 + (frac2/2), "PoolServerWorkload", middleware_ip, middleware_port, message_size)
  _start_multiple_dialog_clients(frac3, frac1 + frac2 + 200, middleware_ip, middleware_port, message_size)


''' client stop methods '''
def _kill_java_processes():
  sudo('killall java')
  time.sleep(5)

def kill_all():
  _kill_java_processes()


''' post processing methods '''
def _copy_client_logs(experiment_id):
  get('~/log', 'log/%s/clients' % experiment_id)


def _copy_server_logs(experiment_id):
  get('~/log', 'log/%s/middleware' % experiment_id)
  get('~/var/config.properties', 'log/%s/middleware' % experiment_id)


def get_client_logs(experiment_id):
  _copy_client_logs(experiment_id)


def get_server_logs(experiment_id):
  _copy_server_logs(experiment_id)


def _process_logs(experiment_id):
  if env.hosts == [MW]:
    path = "log/%s/middleware/log/" % experiment_id
    _snip_last_line(path)
    local('cat log/%s/middleware/log/* | sort -n > log/%s/allmiddlewares.log' % (experiment_id, experiment_id))    
  elif env.hosts == [CL]:
    path = "log/%s/clients/log/" % experiment_id
    _snip_last_line(path)
    local('cat log/%s/clients/log/* | sort -n > log/%s/allclients.log' % (experiment_id, experiment_id))

def _snip_last_line(path):
  files_in_dir = os.listdir(path)
  for file_in_dir in files_in_dir:
    fin = open(path + file_in_dir)
    lines = fin.readlines()
    fin.close()
    fout = open(path + "/" + file_in_dir, 'w')
    fout.writelines(lines[:-1])
    fout.close()

def get_logs():
  _copy_logs()
  _process_client_logs()


def pre_process_logs(experiment_id):
  _process_logs(experiment_id)


def clean_db():
  _truncate_db("flux")


def clean_up():
  run('rm -rf ~/log')
















