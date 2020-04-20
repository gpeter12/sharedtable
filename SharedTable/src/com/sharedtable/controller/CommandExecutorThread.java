package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;


/*
 * Mivel a STCanvas-t egyszerre csak egy thread kezeleheti, így szükséges egy command buffer, ami
 * sorban dolgozza fel a különböző threadekről akár egyszerre beérkező parancsokat. nagyon jól kihasználja a
 * command pattern sajátosságát.
 * */

public class CommandExecutorThread extends Thread {

    @Override
    public void run() {
            while (!timeToStop) {
                try { commandQueue.take().execute(); }
                catch (InterruptedException e) {
                    if (timeToStop == true) {
                        System.out.println("CommandExecutorThread shutting down");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*Platform.runLater(() -> {

                });*/
            }

    }

    public void addCommandToCommandQueue(Command command) {
        try { semaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        if(command == null)
            throw new RuntimeException("null command added to command queue");

        try {
            commandQueue.put(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        semaphore.release();
    }

    public void timeToStop() {
        timeToStop = true;
        interrupt();
    }

    private Semaphore semaphore = new Semaphore(1);
    private BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<Command>(10000);
    private boolean timeToStop = false;
}
