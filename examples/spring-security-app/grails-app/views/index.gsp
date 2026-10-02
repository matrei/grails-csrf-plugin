<!doctype html>
<html lang="en">
<head>
    <title>Public</title>
    <csrf:headToken/>
</head>
<body>
    <form action="/" method="POST">
        <csrf:formToken/>
        <input type="text" name="name">
        <input type="submit" value="Submit">
    </form>
</body>
</html>
