<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>test</title>
	</head>
	<body>
		<h1>hello, ${user.name}.</h1>
        <p>task size:${taskList.size()}</p>
        <div>
            <c:forEach var="task" items="${taskList}">
            <p>${task.getName()}</p>
            </c:forEach>
        </div>
	</body>
</html>
