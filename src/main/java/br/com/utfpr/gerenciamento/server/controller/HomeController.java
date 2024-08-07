package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.dashboards.*;
import br.com.utfpr.gerenciamento.server.service.DashboardService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("dashboard")
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("emprestimo-count-range")
    public DashboardEmprestimoCountRange findDadosEmprestimoCountRange(@RequestParam("dtIni") String dtIni,
                                                                       @RequestParam("dtFim") String dtFim) {
        return dashboardService.findDadosEmprestimoCountRange(DateUtil.parseStringToLocalDate(dtIni),
                DateUtil.parseStringToLocalDate(dtFim)
        );
    }

    @GetMapping("emprestimo-count-day-range")
    public List<DashboardEmprestimoDia> findDadosEmprestimoByDayRange(@RequestParam("dtIni") String dtIni,
                                                                      @RequestParam("dtFim") String dtFim) {
        return dashboardService.findTotalEmprestimoByDia(DateUtil.parseStringToLocalDate(dtIni),
                DateUtil.parseStringToLocalDate(dtFim)
        );
    }

    @GetMapping("itens-mais-emprestados")
    public List<DashboardItensEmprestados> findItensMaisEmprestados(@RequestParam("dtIni") String dtIni,
                                                                    @RequestParam("dtFim") String dtFim) {
        return dashboardService.findItensMaisEmprestados(DateUtil.parseStringToLocalDate(dtIni),
                DateUtil.parseStringToLocalDate(dtFim));
    }

    @GetMapping("itens-mais-adquiridos")
    public List<DashboardItensAdquiridos> findItensMaisAdquiridos(@RequestParam("dtIni") String dtIni,
                                                                  @RequestParam("dtFim") String dtFim) {
        return dashboardService.findItensMaisAdquiridos(DateUtil.parseStringToLocalDate(dtIni),
                DateUtil.parseStringToLocalDate(dtFim));
    }

    @GetMapping("itens-mais-saidas")
    public List<DashboardItensSaidas> findItensMaisSaidas(@RequestParam("dtIni") String dtIni,
                                                          @RequestParam("dtFim") String dtFim) {
        return dashboardService.findItensComMaisSaidas(DateUtil.parseStringToLocalDate(dtIni),
                DateUtil.parseStringToLocalDate(dtFim));
    }
}
