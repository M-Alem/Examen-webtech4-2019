from django.shortcuts import render
from django.http import HttpResponse

def index(request):
    return HttpResponse("Polls index")
# Create your views here.
