import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.osbot.rs07.script.Script;
import javax.swing.BoxLayout;
import java.awt.GridLayout;


@SuppressWarnings("serial")
public class FisherUI extends JFrame {
	private Fisher fisher;
	private Object fisherLock;
	
	private JButton btnCatherby;
	private JButton btnDraynor;
	
	public FisherUI(Fisher parent, final Object lock) {
		super();
		
		fisher = parent;
		fisherLock = lock;
		
		setTitle("Location");
		setVisible(false);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				unlockFisher();
			}
		});
		
		JPanel pnlLocation = new JPanel();
		pnlLocation.setLayout(new GridLayout(2, 1, 0, 8));
		getContentPane().add(pnlLocation);
		
		buttonListener listener = new buttonListener();

		btnDraynor = new JButton("Draynor - Shrimp & Anchovies");
		btnDraynor.addActionListener(listener);
		pnlLocation.add(btnDraynor);
		
		btnCatherby = new JButton("Catherby - Tuna & Swordfish");
		btnCatherby.addActionListener(listener);
		pnlLocation.add(btnCatherby);
		
		pack();
		setVisible(true);
	}
	
	private class buttonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(btnCatherby)) {
				fisher.setMode(Fisher.Mode.CATHERBY_TUNA_SWORDFISH);
			} else if(e.getSource().equals(btnDraynor)) {
				fisher.setMode(Fisher.Mode.DRAYNOR_SHRIMP_ANCHOVIES);
			}
			unlockFisher();
		}
	}
	
	private void unlockFisher() {
		synchronized(fisherLock) {
			fisherLock.notifyAll();
		}
	}
}
