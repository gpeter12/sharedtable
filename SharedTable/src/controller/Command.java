package controller;

import java.util.UUID;

public interface Command {
    void execute();
    void deepCopy(Command command);
    UUID getCreatorID();
}
