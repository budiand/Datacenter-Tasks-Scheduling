/* Implement this class. */

import java.util.List;

public class MyDispatcher extends Dispatcher {


    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }

    // se incepe intotdeauna de la nodul cu id = 0
    private int prev_id = 0;

    @Override
    public synchronized void addTask(Task task) {
        if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {

            hosts.get(prev_id).addTask(task);
            // calculez carui nod trimit taskul
            prev_id = (prev_id + 1) % hosts.size();

        }
        else if (algorithm == SchedulingAlgorithm.SHORTEST_QUEUE) {
            // trimit la nodul cu coada cea mai mica
            Host sqHost = null;
            int min = Integer.MAX_VALUE;

            for (Host host : hosts) {
                int queueSize = host.getQueueSize();
                if (queueSize < min) {
                    min = queueSize;
                    sqHost = host;
                    // daca nodurile au aceeasi dimensiune a cozii, trimit catre cel cu id mai mic
                } else if (queueSize == min && sqHost != null && hosts.indexOf(host) < hosts.indexOf(sqHost)) {
                    // il aleg pe cel cu id mai mic
                    sqHost = host;
                }
            }

            if (sqHost != null) {
                sqHost.addTask(task);
            }
        }
        else if (algorithm == SchedulingAlgorithm.SIZE_INTERVAL_TASK_ASSIGNMENT) {
            // 3 noduri in ordinea small, medium, large
            if (task.getType() == TaskType.SHORT) {
                hosts.get(0).addTask(task);
            } else if (task.getType() == TaskType.MEDIUM) {
                hosts.get(1).addTask(task);
            } else if (task.getType() == TaskType.LONG) {
                hosts.get(2).addTask(task);
            }

        }
        else if (algorithm == SchedulingAlgorithm.LEAST_WORK_LEFT) {
            // compar timpii...
            Host LWLHost = null;
            long min = Integer.MAX_VALUE;
            long threshold = 1000; // millisec

            for (Host host : hosts) {
                long timeLeft = host.getWorkLeft();
                if (timeLeft < min) {
                    min = timeLeft;
                    LWLHost = host;
                } else if (timeLeft - min <= threshold && LWLHost != null 
                        && hosts.indexOf(host) < hosts.indexOf(LWLHost)) {
                            
                    LWLHost = host;
                }
            }

            if (LWLHost != null) {
                LWLHost.addTask(task);
            }
        }
    }
}

