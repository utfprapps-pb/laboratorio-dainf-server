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
    <p class="headerTitulo">Confirmação de Reserva de Materiais</p>
</div>
<div>
    <div class="article">
        <p>Olá ${usuario},</p>
        <br/><br/>
        <p>Sua reserva foi realizada com sucesso no dia ${dtReserva}, e com a data de retirada dos materiais para ${dtRetirada}.</p>
        <br/>
        <p>Segue abaixo a lista dos materiais reservados:</p>
        <br/>
        <table>
            <thead>
            <tr>
                <th scope="col" style="width: 300px;" class="text-left">Item</th>
                <th scope="col">Qtde</th>
            </tr>
            </thead>
            <tbody>
            <#foreach item in reservaItem>
                <tr class="tableBody">
                    <td>${item.item.nome}</td>
                    <td class="text-center">${item.qtde}</td>
                </tr>
            </#foreach>
            </tbody>
        </table>
        <br/>
        <p>Lembre-se de ir até o laboratório para efetuar o devido empréstimo dos materiais reservados.</p>
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
