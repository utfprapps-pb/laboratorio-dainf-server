package br.com.utfpr.gerenciamento.server.component;

import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmprestimoScheduled {

    private final EmprestimoService emprestimoService;

    public EmprestimoScheduled(@Lazy EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @Async
    @Scheduled(cron = "0 0 12 ? * *")
    public void sendEmailPrazoDevolucaoProximo() {
        emprestimoService.sendEmailPrazoDevolucaoProximo();
    }
}
