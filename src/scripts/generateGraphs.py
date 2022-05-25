import pandas as pd
import matplotlib.pyplot as plt
import sys
import os
from pathlib import Path

two_up = str(Path(__file__).resolve().parents[2])
d = two_up.replace('\\', '/')


def plotUnits():
    data = pd.read_csv(
        d + '/Activity per Unit.csv', encoding='unicode_escape', skiprows=1)

    df = pd.DataFrame(data)
    X = list(df.iloc[:, 0])
    Y = list(df.iloc[:, 1])
    plt.bar(X, Y, color=['#66BB6A', '#FF80AB', '#795548'])
    plt.title("Activity per Unit")
    plt.xlabel("Unit Names")
    plt.ylabel("Number of Activities")

    fig1 = plt.gcf()
    plt.draw()
    fig1.savefig('unit.png')
    plt.clf()


def plotHazelResident():
    data = pd.read_csv(
        d + '/Activity per Resident in Hazel.csv', encoding='unicode_escape', skiprows=1)

    df = pd.DataFrame(data)
    X = list(df.iloc[:, 0])
    Y = list(df.iloc[:, 1])
    plt.bar(X, Y, color=['#795548'])
    plt.title("Activity per Resident in Hazel")
    plt.xlabel("Resident Names")
    plt.ylabel("Number of Activities")

    fig1 = plt.gcf()
    plt.draw()
    fig1.savefig('hazel.png')
    plt.clf()


def plotRoseResident():
    data = pd.read_csv(
        d + '/Activity per Resident in Rose.csv', encoding='unicode_escape', skiprows=1)

    df = pd.DataFrame(data)
    X = list(df.iloc[:, 0])
    Y = list(df.iloc[:, 1])
    plt.bar(X, Y, color=['#FF80AB'])
    plt.title("Activity per Resident in Rose")
    plt.xlabel("Resident Names")
    plt.ylabel("Number of Activities")

    fig1 = plt.gcf()
    plt.draw()
    fig1.savefig('rose.png')
    plt.clf()


def plotWillowResident():
    data = pd.read_csv(
        d + '/Activity per Resident in Willow.csv', encoding='unicode_escape', skiprows=1)

    df = pd.DataFrame(data)
    X = list(df.iloc[:, 0])
    Y = list(df.iloc[:, 1])
    plt.bar(X, Y, color=['#66BB6A'])
    plt.title("Activity per Resident in Willow")
    plt.xlabel("Resident Names")
    plt.ylabel("Number of Activities")

    fig1 = plt.gcf()
    plt.draw()
    fig1.savefig('willow.png')
    plt.clf()


if __name__ == '__main__':

    fileName = ['unit.png', 'rose.png', 'hazel.png', 'willow.png']
    for x in fileName:
        if os.path.isfile(x):
            os.remove(x)
        else:
            continue
    plotUnits()
    plotHazelResident()
    plotRoseResident()
    plotWillowResident()
    sys.exit()
