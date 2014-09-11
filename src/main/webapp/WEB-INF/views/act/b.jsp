<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>B's tasks</title>
	</head>
	<body>
		<h1>B's tasks</h1>
        <div>
                <c:forEach var="task" items="${taskList}">
                    <form method="POST">

                    <p>
                        <span>${task.getId()}</span>
                        <input type="hidden" value="${task.getId()}" name="tid">
                        <input type="submit" value="Complete" name="submit" />
                    </p>

                    </form>

                </c:forEach>

        </div>
	</body>
</html>
