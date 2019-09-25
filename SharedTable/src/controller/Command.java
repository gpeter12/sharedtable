package controller;

public interface Command {
    void execute();
    void deepCopy(Command command);
}
