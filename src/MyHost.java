/* Implement this class. */

import java.util.*;

public class MyHost extends Host {

    private Queue<Task> taskQueue = new PriorityQueue<>(new Comparator<Task>() {

        @Override
        public int compare(Task task1, Task task2) {

            // unul dintre taskuri ruleaza <=> durata nu coincide cu timpul ramas
            if(!Objects.equals(task2.getDuration(), task2.getLeft())) {
               // daca task-ul in rulare este preemptibil si task-ul nou este prioritar 
               // atunci task-ul nou va fi adaugat in locul task-ului in rulare
               // altfel, task-ul nou va fi adaugat dupa task-ul in rulare
                if (task2.isPreemptible()) {
                    if(task1.getPriority() > task2.getPriority())
                        return -1;
                    else
                        return 1;
                } else
                // daca task-ul in rulare nu este preemptibil se pastreaza ordinea
                    return 0;
            }

            // altfel, pentru restul task-urilor din coada 
            // se compara dupa prioritate
            else {
                if (task1.getPriority() > task2.getPriority()) {
                    return -1;
                } else if (task1.getPriority() < task2.getPriority()) {
                    return 1;
                    
                // respectiv dupa timpul de start
                } else {
                    if (task1.getStart() < task2.getStart()) {
                        return -1;
                    } else if (task1.getStart() > task2.getStart()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    });


    private volatile boolean exit = false;

    @Override
    public void run() {

        while (!exit) {
            synchronized (taskQueue) {
                try {
                    Task currentTask = taskQueue.peek();
                    if (currentTask != null) {

                        Thread.sleep(100);
                        currentTask.setLeft(currentTask.getLeft() - 100);
                        
                        if (currentTask.getLeft() <= 0) {
                            currentTask.finish();
                            taskQueue.poll();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } 
            }            
        }
    }

    @Override
    public synchronized void addTask(Task task) {
         if(task != null) {
            taskQueue.add(task);
        }
    }

    @Override
    public synchronized int getQueueSize() {
        return taskQueue.size();
    }

    @Override
    public long getWorkLeft() {
        long workLeft = 0;

        for (Task task : taskQueue) {
            workLeft += task.getLeft();
        }
        return workLeft;
    }

    @Override
    public void shutdown() {
        exit = true;
    }
}
