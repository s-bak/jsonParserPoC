package com.jsoncrud;

import com.jsoncrud.menu.MenuHandler;
import com.jsoncrud.repository.JsonFileRepository;
import com.jsoncrud.service.RecordService;

public class Main {

    public static void main(String[] args) {
        JsonFileRepository repo    = new JsonFileRepository("data/records.json");
        RecordService      service = new RecordService(repo);
        MenuHandler        handler = new MenuHandler(service);
        handler.run();
    }
}
