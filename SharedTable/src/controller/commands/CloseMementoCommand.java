/*package controller.commands;

import controller.Command;

import java.util.UUID;

public class CloseMementoCommand implements Command {
    public CloseMementoCommand(UUID mementoID, int commandNumber) {
        this.mementoID = mementoID;
        this.commandNumber = commandNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(22).append(";").append(mementoID).append(";").append(commandNumber);
        return sb.toString();
    }



    public UUID getMementoID() {
        return mementoID;
    }

    public void setMementoID(UUID mementoID) {
        this.mementoID = mementoID;
    }

    public int getCommandNumber() {
        return commandNumber;
    }

    public void setCommandNumber(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    int commandNumber;
    UUID mementoID;

    @Override
    public void execute() {

    }

    @Override
    public void deepCopy(Command command) {

    }
}
*/