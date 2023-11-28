import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DemoThread extends JFrame {

    private volatile boolean isRunning = false;
    private int cloudWidth = 600, cloudHeight = 150, cloudX = 200, rainBankWidth = 600, rainBankHeight = 70,
            rainBankY = (600 - rainBankHeight), rainBankX = 200;

    private static Image background;
    private static Image drop;
    private static Image rainbank;
    private static Image cloud;
    private JButton startButton;
    private JButton stopButton;
    private final List<Drop> drops = new ArrayList<>();

    public DemoThread() {
        setTitle("Demo app");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(new Background());
        Container content = getContentPane();

        startButton = new JButton("Старт");
        startButton.setPreferredSize(new Dimension(100, 50));
        startButton.setBackground(Color.white);
        startButton.setForeground(Color.BLACK);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setVisible(false);
                stopButton.setVisible(true);
                isRunning = true;
                Thread rainMove = new Thread(new RainThread());
                rainMove.start();
                Thread rainMove1 = new Thread(new DropThread());
                rainMove1.start();
            }
        });
        content.add(startButton);

        stopButton = new JButton("Стоп");
        stopButton.setPreferredSize(new Dimension(100, 50));
        stopButton.setBackground(Color.white);
        stopButton.setForeground(Color.BLACK);
        stopButton.setVisible(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopButton.setVisible(false);
                startButton.setVisible(true);
                isRunning = false;
            }
        });
        content.add(stopButton);

        content.add(new CloudRainbank());
    }

    private static class Background extends JPanel {
        private int cloudWidth, cloudHeight, cloudX, rainBankY, rainBankX, rainBankWidth, rainBankHeight;

        public Background() {
            this.cloudWidth = cloudWidth;
            this.cloudHeight = cloudHeight;
            this.cloudX = cloudX;
            this.rainBankY = rainBankY;
            this.rainBankX = rainBankX;
            this.rainBankWidth = rainBankWidth;
            this.rainBankHeight = rainBankHeight;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.drawImage(cloud, cloudX, 0, cloudWidth, cloudHeight, this);
            graphics2D.drawImage(rainbank, rainBankX, rainBankY, rainBankWidth, rainBankHeight, this);
            try {
                background = ImageIO.read(new File("background.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            g.drawImage(background, 0, 0, null);
        }
    }

    private class CloudRainbank extends JPanel {

        public CloudRainbank() {
            setOpaque(false);
            setPreferredSize(new Dimension(1000, 600));
            try {
                cloud = ImageIO.read(new File("cloud.png"));
                rainbank = ImageIO.read(new File("puddle.png"));
                drop = ImageIO.read(new File("drop.png"));
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.drawImage(cloud, cloudX, 0, cloudWidth, cloudHeight, this);
            graphics2D.drawImage(rainbank, rainBankX, rainBankY, rainBankWidth, rainBankHeight, this);

            for (Drop d : drops) {
                d.draw(graphics2D);
            }
        }
    }

    private class Drop {
        private int x, y;

        public Drop(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void move() {
            y += 5;
        }

        public void draw(Graphics2D g) {
            g.drawImage(drop, x, y, 25, 25, null);
        }
    }

    private void generateDrops() {
        int dropX = ThreadLocalRandom.current().nextInt(cloudX, cloudWidth + 1);
        int dropY = ThreadLocalRandom.current().nextInt(cloudHeight, rainBankY + 1);

        Drop newDrop = new Drop(dropX, dropY);
        drops.add(newDrop);
    }

    private class RainThread implements Runnable {
        @Override
        public void run() {
            while (cloudHeight > 0 && isRunning) {
                cloudHeight -= 2;
                rainBankHeight += 2;
                rainBankY = (600 - rainBankHeight);
                cloudX = 200;
                rainBankX = 200;

                SwingUtilities.invokeLater(() -> {
                    repaint();
                    generateDrops();
                });

                try {
                    Thread.sleep(100);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    private class DropThread implements Runnable {
        @Override
        public void run() {
            while (cloudHeight > 0 && isRunning) {
                for (Drop d : drops) {
                    d.move();
                }

                SwingUtilities.invokeLater(() -> repaint());

                try {
                    Thread.sleep(50);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> new DemoThread().setVisible(true));
    }
}
