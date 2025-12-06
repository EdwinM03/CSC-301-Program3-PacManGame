/// MURAT YILDIZ, EDWIN MELGAR
/// CSC-301
/// DR.LORI JACQUES
/// 

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pac-Man");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Our game panel (sets its own preferred size)
            PacMan pacmanGame = new PacMan();
            frame.add(pacmanGame);

            // Size window based on PacMan's preferred size
            frame.pack();
            frame.setLocationRelativeTo(null); // center on screen

            frame.setVisible(true);

            // Make sure Pac-Man panel gets keyboard focus
            pacmanGame.requestFocusInWindow();
        });
    }
}
// -------------------------------------------------------------------------
// Citation
//
// 1)
// Novikov, A., Yakovlev, S., & Gushchin, I. (2025).
// Exploring the possibilities of MADDPG for UAV swarm control
// by simulating in Pac-Man environment.
//
// 2)
// GeeksforGeeks – "Breadth First Search (BFS) Algorithm"
// https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/
// (Used to review BFS graph traversal for Blinky's chase behavior.)
//
// 3)
// GeeksforGeeks – "A* Search Algorithm"
// https://www.geeksforgeeks.org/a-search-algorithm/
// (Referenced for implementing Pinky's A* predictive movement.)
//
// 4)
// Red Blob Games – "Introduction to A* Pathfinding"
// https://www.redblobgames.com/pathfinding/a-star/introduction.html
// (Used to understand heuristics and optimizing pathfinding.)
// -------------------------------------------------------------------------
// -------------------------------------------------------------------------
// Summary
//
// Radioelectronic and Computer Systems, 2025(1), 327–337.
// We added real pathfinding to the ghosts so they no longer
// move randomly. First, we converted the maze into a graph so
// every walkable tile connects to its neighbors. Using that
// graph,we implemented BFS and wired it to Blinky so he directly
// chases Pac-Man, and we implemented A* for Pinky with a target
// four tiles ahead of Pac-Man to make his movement more predictive.
// We also designed Inky to be a hybrid of Blinky and Pinky and now
// use both BFS and A* behavior and Clyde’s distance-based rule system
// to match their classic personalities. Throughout the process, we
// fixed movement alignment, turning logic, freezing issues, and speed
// problems so all ghosts now move smoothly and only turn at intersections.
// -------------------------------------------------------------------------

