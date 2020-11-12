# interview
the answer of assignment #2 for apple


I chose the assignment #2 as the answer which is located in attachment.
the project is written with java,based on SpringBoot framework, you should
install JDK8 and maven and then the project can run correctly.

When the project is running,open http://localhost:8080 on your brower,Chome is preferred.
select the 3 csv files in order and then press "upload",it will show the files are uploaded by "√"

Follow the alert tip,press the "show result",then a new page
comes out that shows the final result.
I didn't write the download function to export the csv named order_execution_plan_horizontal.csv because it is the same as
the page above.
It took me several hours till yesterday midnight for as a managing engineering
developer on working day, I should speed more spirit on training some staff who are intern.
I use the memory database named H2,and you can open http://localhost:8080/h2-console to check the data that comes from
the files you upload or check the final result.
JDBC Driver: org.h2.Driver
JDBC URL:  jdbc:h2:mem:test
username: root
password: test

the below statements are my thought for this assignment:
1.using the Ordering data structer which comes from Guava lib to fore the supply order,the earlier date is in front. The earlier demand date is in front as well.
2.follow the rule named "first-come-first-serve manner",first I fill the large quantity demand order in higher priority. Large demand quantity will be separated to fill the suppliers.
see code"com.apple.interview.service.ResultService#generatePlants".
3.if some demand orders have left quantity,then I will allocate them to the left suppliers.
see code "com.apple.interview.service.ResultService#generateLeftPlants".
4.pivot the column date data into horizon on the result page.
