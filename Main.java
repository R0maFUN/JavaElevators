package com.company;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {
    static int MaxFloor = 10;
    static int MaxWorkload = 5;
    static int ElevatorsAmount = 2;

    public static void main(String[] args) {
        List<Elevator> elevators = new ArrayList<Elevator>();
        List<Request> unhandledRequests = new ArrayList<Request>();
        for (int i = 0; i < ElevatorsAmount; ++i) {
            elevators.add(new Elevator());
            elevators.get(i).start();
        }

        System.out.println("Created " + elevators.size() + " elevators");

        while(true) {
            try {
                sleep(1000);
            } catch(InterruptedException e) { }
            System.out.println("\nCurrent data:");
            for (Elevator el : elevators)
                el.printData();

            System.out.println("Unhandled requests:");
            for (Request unhandled : unhandledRequests)
                unhandled.printData();

            // Handle old requests
            //unhandledRequests.removeIf(req -> RequestHandler.HandleRequest(req, elevators));
            int k = 0;
            while (k < unhandledRequests.size()) {
                Request req = unhandledRequests.get(k);
                if (RequestHandler.HandleRequest(req, elevators)) {
                    unhandledRequests.remove(req);
                    k--;
                }
                k++;
            }

            if (unhandledRequests.size() > 3)
                continue;

            Request newRequest = new Request();
            if (newRequest.status == Status.error)
                continue;

            // Handle new request
            if (!RequestHandler.HandleRequest(newRequest, elevators)) {
                unhandledRequests.add(newRequest);
            }

        }

    }
}

class RequestHandler {
    static boolean HandleRequest(Request req, List<Elevator> elevators) {
        // Find waiting elevators
        for (Elevator elevator : elevators) {
            if (elevator.hasNoRequests()) {
                elevator.processRequest(req);
                return true;
            }
        }

//        // Find elevators that will be waiting
//        for (Elevator elevator : elevators) {
//            if (elevator.getPredictedWorkload(req.currentFloor) == 0) {
//                elevator.processRequest(req);
//                return true;
//            }
//        }

        // Find elevators that will go through requested floor
        for (Elevator elevator : elevators) {
            if (req.direction != elevator.direction)
                continue;
            if (!(req.direction == Direction.up && req.currentFloor >= elevator.lastDestinationFloor) &&
                    !(req.direction == Direction.down && req.currentFloor <= elevator.lastDestinationFloor))
                continue;
            if (elevator.getPredictedWorkload(req.currentFloor) < Main.MaxWorkload) {
                elevator.processRequest(req);
                return true;
            }
        }

        return false;
    }
}