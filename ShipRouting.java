package Shipping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.DefaultWaypoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

class PortNode {
    String name;
    GeoPosition position;
    Map<PortNode, Integer> neighbors;

    public PortNode(String name, GeoPosition position) {
        this.name = name;
        this.position = position;
        this.neighbors = new HashMap<>();
    }

    public void addNeighbor(PortNode neighbor, int distance) {
        neighbors.put(neighbor, distance);
    }
}

public class ShipRouting extends JPanel {
    private JXMapViewer mapViewer;
    private List<PortNode> ports;
    private Queue<GeoPosition> waypointQueue;
    private JLabel shipIcon;
    private JTextArea shipDetailsArea;
    private JComboBox<String> startPortComboBox;
    private JComboBox<String> endPortComboBox;
    private List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private ImageIcon shipIconImage;

    public ShipRouting() {
        setLayout(new BorderLayout());
        mapViewer = new JXMapViewer();

        TileFactoryInfo info = new TileFactoryInfo(
                "OpenStreetMap", 1, 15, 17, 256, true, true,
                "http://tile.openstreetmap.org", "x", "y", "z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17 - zoom;
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        GeoPosition startPosition = new GeoPosition(20.5937, 78.9629); // Centered on India
        mapViewer.setZoom(3);
        mapViewer.setAddressLocation(startPosition);

        // Add interaction listeners
        MouseAdapter mouseAdapter = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mouseAdapter);
        mapViewer.addMouseMotionListener(mouseAdapter);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create ports and routes
        createPortsAndRoutes();

        // UI components for user input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        JLabel startPortLabel = new JLabel("Start Port:");
        JLabel endPortLabel = new JLabel("End Port:");

        startPortComboBox = new JComboBox<>();
        endPortComboBox = new JComboBox<>();

        for (PortNode port : ports) {
            startPortComboBox.addItem(port.name);
            endPortComboBox.addItem(port.name);
        }

        JButton showPathsButton = new JButton("Show Paths");
        showPathsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPaths();
            }
        });

        JButton startJourneyButton = new JButton("Start Shortest Journey");
        startJourneyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startShortestJourney();
            }
        });

        inputPanel.add(startPortLabel);
        inputPanel.add(startPortComboBox);
        inputPanel.add(endPortLabel);
        inputPanel.add(endPortComboBox);
        inputPanel.add(showPathsButton);
        inputPanel.add(startJourneyButton);

        shipDetailsArea = new JTextArea(5, 20);
        shipDetailsArea.setEditable(false);

        add(mapViewer, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(shipDetailsArea), BorderLayout.SOUTH);

        // Add ship icon
        shipIconImage = new ImageIcon("path/to/ship-icon.png"); // Provide the path to your ship icon
        shipIcon = new JLabel(shipIconImage);
        shipIcon.setSize(30, 30);
        mapViewer.add(shipIcon);
    }

    private void createPortsAndRoutes() {
        ports = new ArrayList<>();

        // Adding ports in India (sea shores)
        PortNode indiaMain = new PortNode("India - Mumbai", new GeoPosition(18.9647, 72.8258)); // Mumbai
        PortNode indiaPort1 = new PortNode("India - Chennai", new GeoPosition(13.0827, 80.2707)); // Chennai
        PortNode indiaPort2 = new PortNode("India - Kolkata", new GeoPosition(22.5726, 88.3639)); // Kolkata
        PortNode indiaPort3 = new PortNode("India - Cochin", new GeoPosition(9.9312, 76.2673)); // Cochin
        PortNode indiaPort4 = new PortNode("India - Kandla", new GeoPosition(23.0324, 70.2228)); // Kandla

        // Adding ports in Brazil (sea shores)
        PortNode brazilMain = new PortNode("Brazil - Rio de Janeiro", new GeoPosition(-22.9068, -43.1729)); // Rio de Janeiro
        PortNode brazilPort1 = new PortNode("Brazil - Santos", new GeoPosition(-23.9536, -46.3322)); // Santos
        PortNode brazilPort2 = new PortNode("Brazil - Salvador", new GeoPosition(-12.9714, -38.5014)); // Salvador
        PortNode brazilPort3 = new PortNode("Brazil - Recife", new GeoPosition(-8.0476, -34.8770)); // Recife
        PortNode brazilPort4 = new PortNode("Brazil - Fortaleza", new GeoPosition(-3.7172, -38.5434)); // Fortaleza

        // Adding ports in America (sea shores)
        PortNode americaMain = new PortNode("USA - New York", new GeoPosition(40.7128, -74.0060)); // New York
        PortNode americaPort1 = new PortNode("USA - Los Angeles", new GeoPosition(33.9416, -118.4085)); // Los Angeles
        PortNode americaPort2 = new PortNode("USA - Miami", new GeoPosition(25.7617, -80.1918)); // Miami
        PortNode americaPort3 = new PortNode("USA - Houston", new GeoPosition(29.7604, -95.3698)); // Houston
        PortNode americaPort4 = new PortNode("USA - Seattle", new GeoPosition(47.6062, -122.3321)); // Seattle

        // Adding ports in Canada (sea shores)
        PortNode canadaMain = new PortNode("Canada - Vancouver", new GeoPosition(49.2827, -123.1207)); // Vancouver
        PortNode canadaPort1 = new PortNode("Canada - Halifax", new GeoPosition(44.6488, -63.5752)); // Halifax
        PortNode canadaPort2 = new PortNode("Canada - Montreal", new GeoPosition(45.5017, -73.5673)); // Montreal
        PortNode canadaPort3 = new PortNode("Canada - Toronto", new GeoPosition(43.651070, -79.347015)); // Toronto
        PortNode canadaPort4 = new PortNode("Canada - Quebec City", new GeoPosition(46.8139, -71.2082)); // Quebec City

        // Adding intermediate sea waypoints to avoid land
        PortNode waypoint1 = new PortNode("Waypoint 1", new GeoPosition(12.0, 70.0));
        PortNode waypoint2 = new PortNode("Waypoint 2", new GeoPosition(5.0, 50.0));
        PortNode waypoint3 = new PortNode("Waypoint 3", new GeoPosition(-5.0, 40.0));
        PortNode waypoint4 = new PortNode("Waypoint 4 - Cape Town", new GeoPosition(-33.9258, 18.4232)); // Cape Town
        PortNode waypoint5 = new PortNode("Waypoint 5", new GeoPosition(-25.0, 10.0));
        PortNode waypoint6 = new PortNode("Waypoint 6", new GeoPosition(-20.0, -10.0));
        PortNode waypoint7 = new PortNode("Waypoint 7", new GeoPosition(-10.0, -20.0));
        PortNode waypoint8 = new PortNode("Waypoint 8", new GeoPosition(0.0, -30.0));
        PortNode waypoint9 = new PortNode("Waypoint 9", new GeoPosition(10.0, -40.0));
        PortNode waypoint10 = new PortNode("Waypoint 10", new GeoPosition(0.0, -50.0));
        PortNode waypoint11 = new PortNode("Waypoint 11", new GeoPosition(-10.0, -60.0));
        PortNode waypoint12 = new PortNode("Waypoint 12", new GeoPosition(-25.0, -70.0));

        ports.add(indiaMain);
        ports.add(indiaPort1);
        ports.add(indiaPort2);
        ports.add(indiaPort3);
        ports.add(indiaPort4);

        ports.add(brazilMain);
        ports.add(brazilPort1);
        ports.add(brazilPort2);
        ports.add(brazilPort3);
        ports.add(brazilPort4);

        ports.add(americaMain);
        ports.add(americaPort1);
        ports.add(americaPort2);
        ports.add(americaPort3);
        ports.add(americaPort4);

        ports.add(canadaMain);
        ports.add(canadaPort1);
        ports.add(canadaPort2);
        ports.add(canadaPort3);
        ports.add(canadaPort4);

        ports.add(waypoint1);
        ports.add(waypoint2);
        ports.add(waypoint3);
        ports.add(waypoint4);
        ports.add(waypoint5);
        ports.add(waypoint6);
        ports.add(waypoint7);
        ports.add(waypoint8);
        ports.add(waypoint9);
        ports.add(waypoint10);
        ports.add(waypoint11);
        ports.add(waypoint12);

        // Adding predefined distances
        indiaMain.addNeighbor(indiaPort1, 660);
        indiaMain.addNeighbor(indiaPort2, 860);
        indiaMain.addNeighbor(indiaPort3, 720);
        indiaMain.addNeighbor(indiaPort4, 940);

        brazilMain.addNeighbor(brazilPort1, 210);
        brazilMain.addNeighbor(brazilPort2, 610);
        brazilMain.addNeighbor(brazilPort3, 1220);
        brazilMain.addNeighbor(brazilPort4, 1860);

        americaMain.addNeighbor(americaPort1, 2450);
        americaMain.addNeighbor(americaPort2, 1090);
        americaMain.addNeighbor(americaPort3, 1630);
        americaMain.addNeighbor(americaPort4, 2420);

        canadaMain.addNeighbor(canadaPort1, 3030);
        canadaMain.addNeighbor(canadaPort2, 2320);
        canadaMain.addNeighbor(canadaPort3, 2090);
        canadaMain.addNeighbor(canadaPort4, 2410);

        // Define international sea routes with intermediate waypoints
        indiaMain.addNeighbor(waypoint1, 1100);
        waypoint1.addNeighbor(waypoint2, 1300);
        waypoint2.addNeighbor(waypoint3, 1400);
        waypoint3.addNeighbor(waypoint4, 1500); // Cape Town
        waypoint4.addNeighbor(waypoint5, 1600);
        waypoint5.addNeighbor(waypoint6, 1700);
        waypoint6.addNeighbor(waypoint7, 1800);
        waypoint7.addNeighbor(waypoint8, 1900);
        waypoint8.addNeighbor(waypoint9, 2000);
        waypoint9.addNeighbor(waypoint10, 2100);
        waypoint10.addNeighbor(waypoint11, 2200);
        waypoint11.addNeighbor(waypoint12, 2300);
        waypoint12.addNeighbor(brazilMain, 2400);

        // Add connections from sub-ports to main ports for international connections
        indiaPort1.addNeighbor(indiaMain, 660);
        indiaPort2.addNeighbor(indiaMain, 860);
        indiaPort3.addNeighbor(indiaMain, 720);
        indiaPort4.addNeighbor(indiaMain, 940);

        brazilPort1.addNeighbor(brazilMain, 210);
        brazilPort2.addNeighbor(brazilMain, 610);
        brazilPort3.addNeighbor(brazilMain, 1220);
        brazilPort4.addNeighbor(brazilMain, 1860);

        americaPort1.addNeighbor(americaMain, 2450);
        americaPort2.addNeighbor(americaMain, 1090);
        americaPort3.addNeighbor(americaMain, 1630);
        americaPort4.addNeighbor(americaMain, 2420);

        canadaPort1.addNeighbor(canadaMain, 3030);
        canadaPort2.addNeighbor(canadaMain, 2320);
        canadaPort3.addNeighbor(canadaMain, 2090);
        canadaPort4.addNeighbor(canadaMain, 2410);
    }

    private PortNode getPortByName(String name) {
        for (PortNode port : ports) {
            if (port.name.equals(name)) {
                return port;
            }
        }
        return null;
    }

    private List<GeoPosition> findShortestRoute(PortNode startPort, PortNode endPort) {
        Map<PortNode, Integer> distances = new HashMap<>();
        Map<PortNode, PortNode> previousNodes = new HashMap<>();
        PriorityQueue<PortNode> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (PortNode port : ports) {
            distances.put(port, Integer.MAX_VALUE);
        }
        distances.put(startPort, 0);
        queue.add(startPort);

        while (!queue.isEmpty()) {
            PortNode current = queue.poll();

            if (current.equals(endPort)) {
                break;
            }

            for (Map.Entry<PortNode, Integer> neighborEntry : current.neighbors.entrySet()) {
                PortNode neighbor = neighborEntry.getKey();
                int newDist = distances.get(current) + neighborEntry.getValue();
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<GeoPosition> path = new ArrayList<>();
        for (PortNode at = endPort; at != null; at = previousNodes.get(at)) {
            path.add(at.position);
        }
        Collections.reverse(path);
        return path.size() == 1 && !path.get(0).equals(startPort.position) ? Collections.emptyList() : path;
    }

    private List<GeoPosition> findLongestRoute(PortNode startPort, PortNode endPort) {
        Map<PortNode, Integer> distances = new HashMap<>();
        Map<PortNode, PortNode> previousNodes = new HashMap<>();
        PriorityQueue<PortNode> queue = new PriorityQueue<>((a, b) -> distances.get(b) - distances.get(a));

        for (PortNode port : ports) {
            distances.put(port, Integer.MIN_VALUE);
        }
        distances.put(startPort, 0);
        queue.add(startPort);

        while (!queue.isEmpty()) {
            PortNode current = queue.poll();

            if (current.equals(endPort)) {
                break;
            }

            for (Map.Entry<PortNode, Integer> neighborEntry : current.neighbors.entrySet()) {
                PortNode neighbor = neighborEntry.getKey();
                int newDist = distances.get(current) + neighborEntry.getValue();
                if (newDist > distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<GeoPosition> path = new ArrayList<>();
        for (PortNode at = endPort; at != null; at = previousNodes.get(at)) {
            path.add(at.position);
        }
        Collections.reverse(path);
        return path.size() == 1 && !path.get(0).equals(startPort.position) ? Collections.emptyList() : path;
    }

    private void moveShipToPosition(GeoPosition position) {
        Point2D worldPos = mapViewer.getTileFactory().geoToPixel(position, mapViewer.getZoom());
        Point2D center = mapViewer.getTileFactory().geoToPixel(mapViewer.getAddressLocation(), mapViewer.getZoom());
        int x = (int) (worldPos.getX() - center.getX());
        int y = (int) (worldPos.getY() - center.getY());
        shipIcon.setLocation(x + mapViewer.getWidth() / 2 - shipIcon.getWidth() / 2, y + mapViewer.getHeight() / 2 - shipIcon.getHeight() / 2);
        mapViewer.repaint();
    }

    private void showPaths() {
        String startPortName = (String) startPortComboBox.getSelectedItem();
        String endPortName = (String) endPortComboBox.getSelectedItem();

        if (startPortName == null || endPortName == null || startPortName.equals(endPortName)) {
            JOptionPane.showMessageDialog(this, "Please select valid start and end ports.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PortNode startPort = getPortByName(startPortName);
        PortNode endPort = getPortByName(endPortName);

        List<GeoPosition> shortestRoute = findShortestRoute(startPort, endPort);
        List<GeoPosition> longestRoute = findLongestRoute(startPort, endPort);

        if (shortestRoute.isEmpty() || longestRoute.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No route found between the selected ports.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        waypointQueue = new LinkedList<>(shortestRoute);

        // Display ship details
        shipDetailsArea.setText("Ship Details:\n");
        shipDetailsArea.append("From: " + startPortName + "\n");
        shipDetailsArea.append("To: " + endPortName + "\n");
        shipDetailsArea.append("Shortest Distance: " + calculateTotalDistance(shortestRoute) + " units\n");
        shipDetailsArea.append("Longest Distance: " + calculateTotalDistance(longestRoute) + " units\n");

        // Draw route lines
        drawRouteLines(shortestRoute, longestRoute);
    }

    private void drawRouteLines(List<GeoPosition> shortestRoute, List<GeoPosition> longestRoute) {
        Set<Waypoint> waypoints = new HashSet<>();
        for (GeoPosition position : shortestRoute) {
            waypoints.add(new DefaultWaypoint(position));
        }
        for (GeoPosition position : longestRoute) {
            waypoints.add(new DefaultWaypoint(position));
        }

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        RoutePainter shortestRoutePainter = new RoutePainter(shortestRoute, Color.RED);
        RoutePainter longestRoutePainter = new RoutePainter(longestRoute, Color.BLUE);
        painters.clear();
        painters.add(waypointPainter);
        painters.add(shortestRoutePainter);
        painters.add(longestRoutePainter);

        mapViewer.setOverlayPainter(new CompoundPainter<>(painters));
    }

    private void startShortestJourney() {
        if (waypointQueue == null || waypointQueue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please show paths first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Start animation
        moveShipToPosition(waypointQueue.peek());
        new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!waypointQueue.isEmpty()) {
                    moveShipToPosition(waypointQueue.poll());
                } else {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            }
        }).start();
    }

    private int calculateTotalDistance(List<GeoPosition> positions) {
        int totalDistance = 0;
        for (int i = 1; i < positions.size(); i++) {
            totalDistance += getDistanceBetweenPorts(positions.get(i - 1), positions.get(i));
        }
        return totalDistance;
    }

    private int getDistanceBetweenPorts(GeoPosition pos1, GeoPosition pos2) {
        PortNode port1 = getPortByPosition(pos1);
        PortNode port2 = getPortByPosition(pos2);
        if (port1 != null && port2 != null) {
            return port1.neighbors.getOrDefault(port2, 0);
        }
        return 0;
    }

    private PortNode getPortByPosition(GeoPosition position) {
        for (PortNode port : ports) {
            if (port.position.equals(position)) {
                return port;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ship Routing System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ShipRouting());
            frame.setSize(800, 600);
            frame.setVisible(true);
        });
    }
}

class RoutePainter implements Painter<JXMapViewer> {
    private List<GeoPosition> track;
    private Color color;

    public RoutePainter(List<GeoPosition> track, Color color) {
        this.track = track;
        this.color = color;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        // Convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Set color and stroke for the route
        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        // Draw the route using lines
        for (int i = 0; i < track.size() - 1; i++) {
            Point2D pt1 = map.getTileFactory().geoToPixel(track.get(i), map.getZoom());
            Point2D pt2 = map.getTileFactory().geoToPixel(track.get(i + 1), map.getZoom());
            g.drawLine((int) pt1.getX(), (int) pt1.getY(), (int) pt2.getX(), (int) pt2.getY());
        }

        g.dispose();
    }
}
