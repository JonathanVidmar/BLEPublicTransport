function [ ] = plotMeasurements( file_name, title_name )
test = load(file_name);
fig = figure;
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
title(title_name)
ylabel({'Distance in meters'});
legend('show');
disp '###################'
disp(file_name)
disp '-------------------'
disp 'Kalman with SC:'
error_norm = norm(test(:,2)-test(:,3))
disp 'Kalman:'
error_norm = norm(test(:,2)-test(:,6))
disp 'Android Beacon Library:'
error_norm = norm(test(:,2)-test(:,4))
disp 'Raw:'
error_norm = norm(test(:,2)-test(:,5))
disp '###################'
end

