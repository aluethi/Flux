from __future__ import with_statement
from fabric.api import *
from fabric.contrib.files import exists
import time


env.user = 'ubuntu'
env.key_filename = 'asl.pem'

''' host definitions '''
def middleware():
  env.hosts = ['ec2-54-75-169-53.eu-west-1.compute.amazonaws.com']


def client():
  env.hosts = ['ec2-54-246-39-198.eu-west-1.compute.amazonaws.com']


def database():
  env.hosts = ['ec2-54-170-184-35.eu-west-1.compute.amazonaws.com']


''' installation procedures '''
def _install_base():
  sudo('apt-get update')
  sudo('apt-get upgrade')


def _install_db():
  sudo('apt-get -q -y install postgresql-9.3 postgresql-contrib-9.3')


def _copy_db_config():
  put('var/postgresql.conf', '/etc/postgresql/9.3/main')
  put('var/pg_hba.conf','/etc/postgresql/9.3/main')


def _create_database(db_user, db_pass, db_name):
  put('var/flux.sql', '~/flux.sql')
  #sudo('psql -c "CREATE USER %s WITH NOCREATEDB NOCREATEUSER ENCRYPTED PASSWORD E\'%s\'"' % (db_user, db_pass), user='postgres')
  #sudo('psql -c "CREATE DATABASE %s WITH OWNER %s"' % (db_name, db_user), user='postgres')
  sudo('psql -f ~/flux.sql -h localhost -p 5432 -d %s -U %s' % (db_name, db_user), user='postgres')


def _truncate_db(db_name):
  sudo('psql -c "TRUNCATE MESSAGE, QUEUE, CLIENT;" -d "%s"' % db_name)


def setup_db():
  #_install_base()
  #_install_db()
  #_copy_db_config()
  _create_database("flux", "flux", "flux")


def _install_java():
  _install_base()
  sudo('apt-get -q -y install openjdk-7-jdk')


def _build_locally():
  local('ant dist')


def _deploy_server():
  put('dist/flux-server.jar', '~/flux-server.jar')


def _deploy_client():
  put('dist/flux-client.jar', '~/flux-client.jar')


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


''' experiment control methods '''
def start_server():
  run('java -jar ~/flux-server.jar &')
  time.sleep(10)


def _start_single_client(experiment_id, middelware_ip, middleware_port, experiment, client_id):
  run('java -jar ~/flux-client.jar %s-%s %s %d %s %d &' % (experiment_id, client_id, middelware_ip, middleware_port, experiment, client_id))


def _start_multiple_clients(count, id_offset, experiment_id, middelware_ip, middleware_port, experiment):
  for i in range(0, count):
    _start_single_client(experiment_id, middelware_ip, middleware_port, experiment, id_offset + i)


def _kill_java_processes():
  sudo('killall java')
  time.sleep(5)


def _copy_client_logs(experiment_id):
  client()
  get('~/log', 'log/%s/clients' % experiment_id)


def _copy_server_logs(experiment_id):
  middleware()
  get('~/log', 'log/%s/middleware' % experiment_id)
  get('~/var/config.properties', 'log/%s/middleware' % experiment_id)


def _copy_logs():
  _copy_client_logs()
  _copy_server_logs()


def _process_client_logs(experiment_id):
  local('cat %s/clients/* | sort -n > %s/clients/allclients.log' % (experiment_id, experiment_id))


def get_logs():
  _copy_logs()
  _process_client_logs()


def _process_logs():
  pass


def _clean_up():
  pass
















