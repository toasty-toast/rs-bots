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

/**
 * This class contains a UI that allows the user to choose
 * what fishing the fishing bot will do
 * 
 * @author Brian McDonald
 */
@SuppressWarnings("serial")
public class FisherUI extends JFrame {
	private Fisher fisher;			// instance of the bot we are controlling
	private Object fisherLock;		// synchronization lock provided by bot
	
	private JButton btnCatherby;	// button for Catherby fishing
	private JButton btnDraynor;		// button for Draynor fishing
	
	/**
	 * Create a new UI for choosing what fishing the bot will do
	 * 
	 * @param parent Instance of the bot we are controlling
	 * @param lock Synchronization lock provided by the bot
	 */
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
		
		// release block on bot thread when window is closed
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				unlockFisher();
			}
		});
		
		// main JPanel to hold all buttons
		JPanel pnlLocation = new JPanel();
		pnlLocation.setLayout(new GridLayout(2, 1, 0, 8));
		getContentPane().add(pnlLocation);
		
		// single button listener instance to apply to all buttons
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
	
	/**
	 * Set bot mode when button is clicked
	 * 
	 * Set mode in bot and release block on bot to allow the bot 
	 * to continue execution
	 */
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
	
	/**
	 * Notify the bot that it can proceed with execution
	 */
	private void unlockFisher() {
		synchronized(fisherLock) {
			fisherLock.notifyAll();
		}
	}
}
