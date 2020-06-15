package br.com.utfpr.gerenciamento.server.component;

import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ItemScheduled {

    @Autowired
    private ItemService itemService;

    @Async
    @Scheduled(cron = "0 0 12 ? * *")
    public void sendNotificationItensAtingiramQtdeMin() {
        itemService.sendNotificationItensAtingiramQtdeMin();
    }
}
