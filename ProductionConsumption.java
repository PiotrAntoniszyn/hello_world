
/* 
 *  Problem producenta i konsumenta
 *	Program ProductionConsumption
 *  Autor: Piotr Antoniszyn, 230503,TN/17:15
 *  Data: 6-12-2016
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ProductionConsumption extends JFrame implements ActionListener {

	JTextArea sim = new JTextArea();
	JScrollPane simScroll;
	JLabel runSimLabel = new JLabel("Symulacja");

	JLabel consumptionLabel = new JLabel("Iloœæ konsumentów:");
	JComboBox consumptionCB = new JComboBox();
	JLabel productionLabel = new JLabel("Iloœæ producentów:");
	JComboBox productionCB = new JComboBox();
	JLabel bufferLabel = new JLabel("Bufor:");
	JComboBox bufferCB = new JComboBox();

	JButton startBtn = new JButton("Rozpocznij");
	JButton stopBtn = new JButton("Wstrzymaj");
	JButton resumeBtn = new JButton("Wznów");

	Bufor x = new Bufor();

	private int buff[] = { 1, 2, 3, 4, 5 };
	private int prod[] = { 1, 2, 3, 4, 5 };

	Producent production[];
	Konsument consumption[];

	ProductionConsumption() {
		super("Production - Consumption");
		setSize(720, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(null);

		sim.setEditable(false);
		sim.setLineWrap(true);
		sim.setToolTipText("Symulacja - przebieg");

		simScroll = new JScrollPane(sim);
		simScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		runSimLabel.setBounds(10, 500, 120, 25);
		simScroll.setBounds(140, 100, 400, 200);

		consumptionLabel.setBounds(370, 20, 120, 20);
		consumptionCB.setBounds(500, 20, 40, 20);
		productionLabel.setBounds(120, 20, 120, 20);
		productionCB.setBounds(230, 20, 120, 20);
		bufferLabel.setBounds(20, 20, 150, 20);
		bufferCB.setBounds(60, 20, 40, 20);

		startBtn.setBounds(140, 320, 100, 20);
		stopBtn.setBounds(280, 320, 100, 20);
		resumeBtn.setBounds(420, 320, 100, 20);

		startBtn.addActionListener(this);
		stopBtn.addActionListener(this);
		resumeBtn.addActionListener(this);

		for (int x : buff) {
			bufferCB.addItem(x);
		}
		for (int x : prod) {
			productionCB.addItem(x);
			consumptionCB.addItem(x);
		}

		panel.add(runSimLabel);
		panel.add(simScroll);

		panel.add(consumptionLabel);
		panel.add(consumptionCB);
		panel.add(productionLabel);
		panel.add(productionCB);
		panel.add(bufferLabel);
		panel.add(bufferCB);

		panel.add(startBtn);
		panel.add(stopBtn);
		panel.add(resumeBtn);
		setContentPane(panel);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String consList, prodList, buffList;
		if (source == startBtn) {
			consList = consumptionCB.getSelectedItem().toString();
			prodList = productionCB.getSelectedItem().toString();
			buffList = bufferCB.getSelectedItem().toString();

			consumption = new Konsument[Integer.parseInt(consList)];
			production = new Producent[Integer.parseInt(prodList)];
			x.setSize(Integer.parseInt(buffList));

			for (int i = 0; i < Integer.parseInt(prodList); i++) {
				production[i] = new Producent(x, i + 1);
			}
			for (int i = 0; i < Integer.parseInt(consList); i++) {
				consumption[i] = new Konsument(x, i + 1);
			}
			for (Producent x : production)
				x.start();
			for (Konsument x : consumption)
				x.start();
		} else if (source == stopBtn) {
			for (Producent x : production) {
				synchronized (x) {
					x.wait = true;
				}
			}
			for (Konsument x : consumption) {
				synchronized (x) {
					x.wait = true;
				}
			}
		} else if (source == resumeBtn) {
			for (Producent x : production) {
				synchronized (x) {
					x.wait = false;
					x.notify();
				}
			}
			for (Konsument x : consumption) {
				synchronized (x) {
					x.wait = false;
					x.notify();
				}
			}
		}
	}

	public static void main(String[] args) {
		new ProductionConsumption();
	}

	class Producent extends Thread {
		char item = 'A';

		Bufor buf;
		int nr;
		boolean wait = false;

		public Producent(Bufor c, int number) {
			buf = c;
			this.nr = number;
		}

		public void run() {
			char c;
			while (true) {
				c = item++;
				buf.put(nr, c);
				synchronized (this) {
					while (wait)
						try {
							wait();
						} catch (InterruptedException e) {
						}
				}
				try {
					sleep((int) (Math.random() * 1000));
				} catch (InterruptedException e) {
				}
			}
		}
	}

	class Konsument extends Thread {
		Bufor buf;
		int nr;
		boolean wait = false;

		public Konsument(Bufor c, int number) {
			buf = c;
			this.nr = number;
		}

		public void run() {
			while (true) {
				buf.get(nr);
				synchronized (this) {
					while (wait)
						try {
							wait();
						} catch (InterruptedException e) {
						}
				}
				try {
					sleep((int) (Math.random() * 1000));
				} catch (InterruptedException e) {
				}
			}
		}
	}

	class Bufor {

		private char contents;
		private int ready = 0;
		private int size;

		public void setSize(int size) {
			this.size = size;
		}

		public synchronized int get(int consu) {
			sim.append("Konsument #" + consu + " chce zabraæ\n");
			System.out.println("Konsument #" + consu + " chce zabraæ");
			while (ready == 0) {
				try {
					System.out.println("Konsument #" + consu + " bufor pusty - czekam");
					wait();
				} catch (InterruptedException e) {
				}
			}
			ready--;
			sim.append("Konsument #" + consu + " zabra³: " + contents + "\n");
			System.out.println("Konsument #" + consu + " zabra³: " + contents);
			notifyAll();
			return contents;
		}

		public synchronized void put(int prod, char value) {
			sim.append("Producent #" + prod + " chce oddaæ: " + value + "\n");
			System.out.println("Producent #" + prod + " chce oddaæ: " + value);
			while (ready == size) {
				sim.append("Producent #" + prod + " bufor zajêty - czekam");
				try {
					System.out.println("Producent #" + prod + " bufor zajêty - czekam");
					wait();
				} catch (InterruptedException e) {
				}
			}
			contents = value;
			ready++;
			sim.append("Producent #" + prod + " odda³: " + value + "\n");
			System.out.println("Producent #" + prod + " odda³: " + value);
			notifyAll();
		}
	}
}
