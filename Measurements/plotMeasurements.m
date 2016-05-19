clear
test = load('test_3.txt');
plot1 = plot(test(:,1),test(:,2));
hold on
plot2 = plot(test(:,1),test(:,3));
plot5 = plot(test(:,1),test(:,6));
plot3 = plot(test(:,1),test(:,4));
plot4 = plot(test(:,1),test(:,5));
set(plot1, 'DisplayName','Actual distance');
set(plot2, 'DisplayName','Kalman with self correction','LineStyle','--');
set(plot3, 'DisplayName','Android Beacon Library','Marker','.');
set(plot4, 'DisplayName','Raw','Marker','*');
set(plot5, 'DisplayName','Kalman','LineStyle','-.');
xlabel({'Time in seconds'});
title({'Distance evaluation of different implementations'});
ylabel({'Distance in meters'});
legend('show');