<html>
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
    <p class="headerTitulo">Finalização da Reserva de Materiais</p>
</div>
<div>
    <div class="article">
        <p>Olá ${usuario},</p>
        <br/><br/>
        <p>A sua reserva realizada no dia ${dtReserva} foi finalizada.</p>
        <br/>
        <p>Em instantes você receberá um email com os dados do empréstimo proveniente da reserva.</p>
        <br/><br/>
        <p>Att,</p>
        <p>Laboratório do Departamento de Informática - UTFPR/PB</p>
    </div>
</div>

<footer class="footer">
    <p class="footerP">Laboratório de Informática - UTFPR/PB </p>
</footer>
</body>
</html>
