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
    <p class="headerTitulo">Confirmação de Empréstimo</p>
</div>
<div>
    <div class="article">
        <p>Olá ${usuarioEmprestimo},</p>
        <br/><br/>
        <p>Seu empréstimo foi realizado com sucesso, no dia ${dtEmprestimo} e com prazo de devolução para ${dtPrazoDevolucao}.</p>
        <br/>
        <p>Segue abaixo a lista dos materiais emprestados:</p>
        <br/>
        <table>
            <thead>
            <tr>
                <th scope="col" style="width: 300px;" class="text-left">Item</th>
                <th scope="col">Qtde</th>
            </tr>
            </thead>
            <tbody>
            <#foreach item in emprestimoItem>
                <tr class="tableBody">
                    <td>${item.item.nome}</td>
                    <td class="text-center">${item.qtde}</td>
                </tr>
            </#foreach>
            </tbody>
        </table>
        <br/><br/>
        <p>Att,</p>
        <p>${usuarioResponsavel}</p>
    </div>
</div>

<footer class="footer">
    <p class="footerP">Laboratório de Informática - UTFPR/PB </p>
</footer>
</body>
</html>
