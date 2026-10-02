<!doctype html>
<html lang="en">
<head>
    <title>Secure</title>
    <csrf:headToken/>
</head>
<body>
    <form action="/secure" method="POST">
        <csrf:formToken/>
        <input type="text" name="name">
        <input type="submit" value="Submit">
    </form>
</body>
</html>
