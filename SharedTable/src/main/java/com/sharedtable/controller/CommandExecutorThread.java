package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;
import javafx.application.Platform;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class CommandExecutorThread extends Thread {

    private Semaphore semaphore = new Semaphore(1);
    private BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<Command>(10000);
    private boolean timeToStop = false;

    @Override
    public void run() {
            while (!timeToStop) {
                try {
                    Command command = commandQueue.take();
                    Platform.runLater(command::execute);
                }
                catch (InterruptedException e) {
                    if (timeToStop) {
                        System.out.println("CommandExecutorThread shutting down");
                    }
                } catch (IllegalStateException e) {
                    //UnitTestnél fordul elő, ignorálni kell.
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

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


}
