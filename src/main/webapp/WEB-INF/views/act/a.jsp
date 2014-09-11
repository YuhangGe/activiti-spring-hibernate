<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>A's tasks</title>
	</head>
	<body>
		<h1>A's tasks</h1>
        <div>
            <p>Total runing process: ${processList.size()}</p>
            <c:forEach var="process" items="${processList}">
                <p>
                    <span>Process:</span>
                    <span>${process.getId()}</span>
                </p>
            </c:forEach>
        </div>
        <form method="POST">
            <div>
                Choice:
                <input name="choice" value="" placeholder="choice:b or c"/>
                <input name="submit" value="Submit" type="submit"/>
            </div>
        </form>
	</body>
</html>
