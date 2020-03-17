package com.sharedtable.controller;

import java.util.UUID;

public interface Command {
    void execute();

    UUID getCreatorID();
}
