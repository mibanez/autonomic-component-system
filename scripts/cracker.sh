#!/bin/bash

acsHome=${HOME}/memoria/autonomic-component-system
proactiveHome=${HOME}/memoria/programming-multiactivities

acsCP=${acsHome}/target/lib/*:${acsHome}/core/target/*:${acsHome}/core/target/lib/*
acsCP=${acsCP}:${acsHome}/examples/target/*:${acsHome}/examples/target/lib/*
acsCP=${acsCP}:${proactiveHome}/dist/lib/*

app=cl.niclabs.scada.acs.examples.cracker.HerculesApp
vm=-Djava.security.manager
vm="${vm} -Djava.security.policy=${proactiveHome}/dist/proactive.java.policy"

java -cp $acsCP $vm $app

