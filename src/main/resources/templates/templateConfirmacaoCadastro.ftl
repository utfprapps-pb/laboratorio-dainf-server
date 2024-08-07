<!DOCTYPE html>
<html  lang="pt-BR">
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
    <title>Laboratório DAINF-PB da UTFPR Campus Pato Branco</title>
</head>
<body>
    <div class="header">
        <p class="headerTitulo">Confirmação de cadastro</p>
    </div>
    <div>
        <div class="article">
            <p>Olá ${usuario},</p>
            <br/><br/>
            <p>Para confirmar o seu cadastro acesse clique em:</p>
            <a th:href="${url}">Confirmar Cadastro</a>
            <br/>
            <p>Ou copie e cole o endereço a seguir no seu navegador: ${url}</p>
            <br/>
            <p>Qualquer dúvida, entrar em contato conosco na sala de apoio do bloco V.</p>
            <br/><br/>
            <p>Atenciosamente,</p>
            <p>Equipe do laboratório do DAINF-PB</p>
        </div>
    </div>

    <footer class="footer">
        <p class="footerP">Laboratório de Informática - UTFPR-PB</p>
    </footer>
</body>
</html>
