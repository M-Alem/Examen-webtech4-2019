from django.shortcuts import render
from django.http import HttpResponse
from datetime import datetime

def index(request):
    #set the date and time format
    date_format = "%m-%d-%Y %H:%M:%S"
    
    #convert string to actual date and time
    time1  = datetime.strptime('3-29-2019 00:00:00', date_format)
    time2  = datetime.strptime('8-02-2008 01:30:00', date_format)
    
    #find the difference between two dates
    diff = time2 - time1
    
    
    ''' days and overall hours between two dates '''
    print ('Days & Overall hours from the above two dates')
    #print days
    days = diff.days
    print (str(days) + ' day(s)')
    
    #print overall hours
    days_to_hours = days * 24
    diff_btw_two_times = (diff.seconds) / 3600
    overall_hours = days_to_hours + diff_btw_two_times
    print (str(overall_hours) + ' hours');
    
    
    
    ''' now print only the time difference '''
    ''' between two times (date is ignored) '''
    
    print ('\nTime difference between two times (date is not considered)')
    
    #like days there is no hours in python
    #but it has seconds, finding hours from seconds is easy
    #just divide it by 3600
    
    hours = (diff.seconds) / 3600  
    print (str(hours) + ' Hours')
    
    
    #same for minutes just divide the seconds by 60
    
    minutes = (diff.seconds) / 60
    print (str(minutes) + ' Minutes')
    
    #to print seconds, you know already ;)
    
    print (str(diff.seconds) + ' secs')
    return HttpResponse(diff)
