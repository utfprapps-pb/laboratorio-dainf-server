<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <style>
        * {
            margin: 0;
            padding: 0;
            font-family: sans-serif;
        }

        body {
            margin: 10px;
        }

        .header {
            width: 100%;
            height: 50px;
            background: linear-gradient(to right, #5180d6, #094288);
            border-radius: 5px 5px 0 0;
        }

        .headerTitulo {
            font-weight: bold;
            padding: 20px;
            color: #FFF;
        }

        .article {
            padding: 30px;
            background-color: #f5f5f5;
            color: #333;
        }

        .footer {
            padding: 5px;
            border-radius: 0 0 5px 5px;
            background: linear-gradient(to right, #5180d6, #094288);
        }

        .footerP {
            font-size: 12px;
            font-family: sans-serif;
            color: #ffffff;
            text-align: center;
        }

        .text-center {
            text-align: center;
        }

        .text-left {
            text-align: left;
        }
    </style>
</head>
<body>
<div class="header">
    <p class="headerTitulo">Confirmação da alteração do prazo de devolução do empréstimo</p>
</div>
<div>
    <div class="article">
        <p>Olá <span th:text="${usuarioEmprestimo}">Usuário</span>,</p>
        <br/><br/>
        <p>O seu empréstimo realizado no dia <span th:text="${dtEmprestimo}">data do empréstimo</span> recebeu um novo prazo de devolução, a nova data é <span th:text="${dtPrazoDevolucao}">data de devolução</span>.</p>
        <br/>
        <p>Qualquer dúvida, entrar em contato conosco.</p>
        <br/><br/>
        <p>Att,</p>
        <p><span th:text="${usuarioResponsavel}">Usuário Responsável</span></p>
    </div>
</div>

<footer class="footer">
    <p class="footerP">Laboratório de Informática - UTFPR/PB</p>
</footer>
</body>
</html>
