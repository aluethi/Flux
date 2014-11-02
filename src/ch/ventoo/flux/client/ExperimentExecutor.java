package ch.ventoo.flux.client;

import ch.ventoo.flux.profiling.BenchLogger;

/**
 * Created by nano on 02/11/14.
 */
public class ExperimentExecutor {

    private static String EXP_CLASS_PATH = "ch.ventoo.flux.client.experiment.";
    private MessageService _service;
    private Experiment _exp;

    public ExperimentExecutor(String host, int port, BenchLogger log) {
        _service = new MessageService(host, port, log);
    }

    public void executeExperiment(String exp, String[] args) {
        try {
            String classPath = EXP_CLASS_PATH + exp;
            Class<?> cls = ExperimentExecutor.class.getClassLoader().loadClass(classPath);
            Experiment experiment = (Experiment) cls.newInstance();
            executeExperiment(experiment, args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void executeExperiment(Experiment exp, String[] args) {
        _exp = exp;
        _exp.start(_service, args);
    }

    public void shutdown() {
        if(_exp != null) {
            _exp.shutdown();
        }
    }

}
