package org.hyperion.hypercon.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Transient;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.hyperion.hypercon.SshConnectionModel;
import org.hyperion.hypercon.spec.SshConfig;
import org.hyperion.ssh.PiSshConnection;

import com.jcraft.jsch.JSchException;

/**
 * @author Fabian Hertwig
 * 
 */
public class SshConnectionPanel extends JPanel implements Observer {

	private final SshConfig mSshConfig;
	private final SshConnectionModel sshConnection;

	private final SshConnectionPanel self;

	private JLabel mAddressLabel;
	private JTextField mAddressField;

	private JLabel mPortLabel;
	private JSpinner mTcpPortSpinner;

	private JLabel mUsernameLabel;
	private JTextField mUsernameField;

	private JLabel mPasswordLabel;
	private JPasswordField mPasswordField;

	private JButton connectBut;

	/**
	 * Constructor
	 * 
	 * @param pSshConfig
	 */
	public SshConnectionPanel(final SshConfig pSshConfig) {
		super();

		self = this;
		mSshConfig = pSshConfig;
		sshConnection = SshConnectionModel.getInstance();
		sshConnection.addObserver(this);

		initialise();
	}

	@Override
	@Transient
	public Dimension getMaximumSize() {
		Dimension maxSize = super.getMaximumSize();
		Dimension prefSize = super.getPreferredSize();
		return new Dimension(maxSize.width, prefSize.height);
	}

	/**
	 * Create, add and layout Gui elements
	 */
	private void initialise() {
		setBorder(BorderFactory.createTitledBorder("Ssh Connection"));

		mAddressLabel = new JLabel("Raspberry IP:");
		add(mAddressLabel);

		mAddressField = new JTextField(mSshConfig.adress);
		mAddressField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				mSshConfig.adress = mAddressField.getText();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				mSshConfig.adress = mAddressField.getText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				mSshConfig.adress = mAddressField.getText();
			}
		});
		add(mAddressField);

		mPortLabel = new JLabel("Port:");
		add(mPortLabel);

		mTcpPortSpinner = new JSpinner(new SpinnerNumberModel(mSshConfig.port, 1, 65535, 1));
		mTcpPortSpinner.addChangeListener(mChangeListener);
		add(mTcpPortSpinner);

		mUsernameLabel = new JLabel("Username:");
		add(mUsernameLabel);

		mUsernameField = new JTextField(mSshConfig.username);
		mUsernameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				mSshConfig.username = mUsernameField.getText();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				mSshConfig.username = mUsernameField.getText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				mSshConfig.username = mUsernameField.getText();
			}
		});
		add(mUsernameField);

		mPasswordLabel = new JLabel("Password:");
		
		add(mPasswordLabel);

		mPasswordField = new JPasswordField(mSshConfig.password);
		mPasswordField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				mSshConfig.password = new String(mPasswordField.getPassword());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				mSshConfig.password = new String(mPasswordField.getPassword());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				mSshConfig.password = new String(mPasswordField.getPassword());
			}
		});
		add(mPasswordField);

		connectBut = new JButton("Connect");
		connectBut.addActionListener(mActionListener);

		

		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		setLayout(layout);

		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
						.addComponent(mAddressLabel)
						.addComponent(mPortLabel)
						.addComponent(mUsernameLabel)
						.addComponent(mPasswordLabel)
						.addComponent(connectBut)
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(mAddressField)
						.addComponent(mTcpPortSpinner)
						.addComponent(mUsernameField)
						.addComponent(mPasswordField)
				));
		
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(mAddressLabel).addComponent(mAddressField))
				.addGroup(layout.createParallelGroup()
						.addComponent(mPortLabel)
						.addComponent(mTcpPortSpinner))
				.addGroup(layout.createParallelGroup()
						.addComponent(mUsernameLabel)
						.addComponent(mUsernameField))
				.addGroup(layout.createParallelGroup()
						.addComponent(mPasswordLabel)
						.addComponent(mPasswordField))
						.addGap(10)
				.addGroup(layout.createParallelGroup()
						.addComponent(connectBut)
						)
					
						);

	}

	/**
	 * Save selection changes i the config file
	 */
	private final ChangeListener mChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			mSshConfig.port = (Integer) mTcpPortSpinner.getValue();
		}
	};

	private final ActionListener mActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == connectBut) {
				if (!sshConnection.isConnected()) {
					setConnectionFieldsAcces(false);
					connectBut.setEnabled(false);
					
					try {
						if(!sshConnection.connect(mAddressField.getText(), (Integer) mTcpPortSpinner.getValue(), mUsernameField.getText(),
								mPasswordField.getPassword())){
							JOptionPane.showMessageDialog(self, "Can't connect. Is all data correct?");
							setConnectionFieldsAcces(true);
						}
						connectBut.setEnabled(true);
					} catch (com.jcraft.jsch.JSchException e1) {
						
						// FIXME: somehow this com.jcraft.jsch.JSchException
						// cant be caught
					}
				} else {
					sshConnection.disconnect();

				}
			}

		}
	};

	private void setConnectionFieldsAcces(boolean setEnabled) {
		mAddressField.setEnabled(setEnabled);
		mTcpPortSpinner.setEnabled(setEnabled);
		mUsernameField.setEnabled(setEnabled);
		mPasswordField.setEnabled(setEnabled);

	}

	@Override
	public void update(Observable o, Object arg) {
		if (SshConnectionModel.getInstance().isConnected()) {
			setConnectionFieldsAcces(false);
			connectBut.setText("Disconnect");
		} else {
			setConnectionFieldsAcces(true);
			connectBut.setText("Connect");
		}

	}
}
