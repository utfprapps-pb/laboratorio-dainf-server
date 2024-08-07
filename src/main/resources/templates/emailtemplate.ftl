<!DOCTYPE html>
<html  lang="pt_BR">
<head>
    <meta charset="UTF-8">
    <title>Email Template</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f8f8;
            border-radius: 10px;
        }
        h1 {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
            color: #5c5c5c;
            margin-bottom: 20px;
        }
        p {
            font-size: 16px;
            line-height: 1.5;
            color: #333;
        }
        .image-container {
            text-align: center;
            margin-top: 20px;
        }
        img {
            max-width: 100%;
            height: auto;
            border-radius: 5px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>${subject}</h1>
    <p>${content}</p>
</div>
</body>
</html>
