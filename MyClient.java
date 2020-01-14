import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.image.*;	//�摜�����ɕK�v
import java.awt.geom.*;		//�摜�����ɕK�v
import java.awt.Dialog.*;

public class MyClient extends JFrame implements MouseListener, MouseMotionListener {
	// �I�Z�����Z�{�[�h�쐬
	private JButton buttonArray[][];

	// �����̃R�}�̐F
	private int myColor;
	// �����̃^�[�����ǂ���
	private int myTurn;
	// �A�C�R���������̂��G�̂�	
	private ImageIcon myIcon, yourIcon;
	// �R���e�i���쐬
	private Container c;
	// �A�C�R����ݒ�
	private ImageIcon blackIcon, whiteIcon, boardIcon;
	// �o�͗p�̃��C�^�[
	PrintWriter out;
	// boardIcon�̐����i�[
	private int fin = 60;

	// ���s����_�C�A���O
	WinDialogWindow dlg = new WinDialogWindow(this);



	public MyClient() {
		// ���O�̓��̓_�C�A���O���J��
		String myName = JOptionPane.showInputDialog(null,"���O����͂��Ă�������","���O�̓���",JOptionPane.QUESTION_MESSAGE);
		if (myName.equals("")) {
			// ���O���Ȃ��Ƃ���"No name"
			myName = "No name";
		}

		//�E�B���h�E���쐬����
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//�@�E�B���h�E�����Ƃ��ɁC����������悤�ɐݒ肷��
		this.setTitle("MyClient");								//�@�E�B���h�E�̃^�C�g����ݒ肷��
		this.setSize(600,500);									//�@�E�B���h�E�̃T�C�Y��ݒ肷��
		c = getContentPane();									//�@�t���[���̃y�C�����擾����

		//�A�C�R���̐ݒ�
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");
		
		//�@�������C�A�E�g�̐ݒ���s��Ȃ�
		c.setLayout(null);
		buttonArray = new JButton[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				buttonArray[j][i] = new JButton(boardIcon);	//�@�{�^���ɃA�C�R����ݒ肷��
				c.add(buttonArray[j][i]);					//�@�y�C���ɓ\��t����
				buttonArray[j][i].setBounds(i*50+10,j*50+10,50,50);	//�@�{�^���̑傫���ƈʒu��ݒ肷��D(x���W�Cy���W,x�̕�,y�̕��ji*45,10,45,45
				buttonArray[j][i].addMouseListener(this);			//�@�{�^�����}�E�X�ł�������Ƃ��ɔ�������悤�ɂ���
				buttonArray[j][i].addMouseMotionListener(this);		//�@�{�^�����}�E�X�œ��������Ƃ����Ƃ��ɔ�������悤�ɂ���
				buttonArray[j][i].setActionCommand(Integer.toString(i*8+j));	//�@�{�^���ɔz��̏���t������i�l�b�g���[�N����ăI�u�W�F�N�g�����ʂ��邽�߁j
			}
		}
		// �I�Z����4�R�}�Z�b�g
		buttonArray[3][3].setIcon(whiteIcon);
		buttonArray[4][4].setIcon(whiteIcon);
		buttonArray[3][4].setIcon(blackIcon);
		buttonArray[4][3].setIcon(blackIcon);
		
		//�T�[�o�ɐڑ�����
		Socket socket = null;
		try {
			// "localhost"�́C���������ւ̐ڑ��Dlocalhost��ڑ����IP Address�i"133.42.155.201"�`���j�ɐݒ肷��Ƒ���PC�̃T�[�o�ƒʐM�ł���
			// 10000�̓|�[�g�ԍ��DIP Address�Őڑ�����PC�����߂āC�|�[�g�ԍ��ł���PC�㓮�삷��v���O��������肷��
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("�z�X�g�� IP �A�h���X������ł��܂���: " + e);
		} catch (IOException e) {
			 System.err.println("�G���[���������܂���: " + e);
		}
		MesgRecvThread mrt = new MesgRecvThread(socket, myName);	// ��M�p�̃X���b�h���쐬����
		mrt.start();												// �X���b�h�𓮂����iRun�������j
	}

	// ���s�̃_�C�A���O(�����ƈ��������̉摜�͗p�ӂł��Ă��Ȃ�)
	class WinDialogWindow extends JDialog implements ActionListener {
		JButton theButton = new JButton();
		ImageIcon thewinImage = new ImageIcon("win.jpg");
		ImageIcon theloseImage = new ImageIcon("lose.jpg");
		ImageIcon thedrawImage = new ImageIcon("draw.jpg");
		JFrame own = new JFrame();
		Container cc = this.getContentPane();
		
		WinDialogWindow(JFrame owner) {
			super(owner);
			own = owner;
			cc.setLayout(null);
		}

		public void win(){
			theButton.setIcon(thewinImage);
			theButton.setBounds(0,0,526,234);
			theButton.addActionListener(this);
			cc.add(theButton);
			setTitle("You Win!");
			setSize(526, 234);
			setResizable(false);
			setUndecorated(true);
			setModal(true);
			setLocation(own.getBounds().x+own.getWidth()/2-this.getWidth()/2,own.getBounds().y+own.getHeight()/2-this.getHeight()/2);
			setVisible(true);
		}

		public void lose(){
			theButton.setIcon(theloseImage);
			theButton.setBounds(0,0,526,234);
			theButton.addActionListener(this);
			cc.add(theButton);
			setTitle("You lose..");
			setSize(526, 234);
			setResizable(false);
			setUndecorated(true);
			setModal(true);
			setLocation(own.getBounds().x+own.getWidth()/2-this.getWidth()/2,own.getBounds().y+own.getHeight()/2-this.getHeight()/2);
			setVisible(true);
		}

		public void draw(){
			theButton.setIcon(thedrawImage);
			theButton.setBounds(0,0,526,234);
			theButton.addActionListener(this);
			cc.add(theButton);
			setTitle("draw");
			setSize(526, 234);
			setResizable(false);
			setUndecorated(true);
			setModal(true);
			setLocation(own.getBounds().x+own.getWidth()/2-this.getWidth()/2,own.getBounds().y+own.getHeight()/2-this.getHeight()/2);
			setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			this.dispose();
		}
	}
		
	//���b�Z�[�W��M�̂��߂̃X���b�h
	public class MesgRecvThread extends Thread {
		Socket socket;
		String myName;
		
		public MesgRecvThread(Socket s, String n){
			socket = s;
			myName = n;
		}

		// �ʐM�󋵂��Ď����C��M�f�[�^�ɂ���ē��삷��
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);	// �ڑ��̍ŏ��ɖ��O�𑗂�
				myTurn = 0;				// �^�[����ݒ�
				String myNumberStr = br.readLine();
				int myNumberInt = Integer.parseInt(myNumberStr);
				int contPass = 0;
				// ��s��U�ƃR�}�̐F�ݒ�
				if (myNumberInt % 2 == 0) {
					myColor = 0;
					myIcon = blackIcon;
					yourIcon = whiteIcon;
				} else {
					myColor = 1;
					myIcon = whiteIcon;
					yourIcon = blackIcon;
				}
				
				while (true) {
					String inputLine = br.readLine();	// �f�[�^����s�������ǂݍ���ł݂�
					if (inputLine != null) {			// �ǂݍ��񂾂Ƃ��Ƀf�[�^���ǂݍ��܂ꂽ���ǂ������`�F�b�N����
						String[] inputTokens = inputLine.split(" ");	// ���̓f�[�^����͂��邽�߂ɁA�X�y�[�X�Ő؂蕪����
						String cmd = inputTokens[0];					// �R�}���h�̎��o���D�P�ڂ̗v�f�����o��
						
						// �N���b�N�����ꏊ�ɒu�������Ɏ��s(PLASE)
						if (cmd.equals("PLACE")) {
							// �{�^���ԍ��擾
							String BName = inputTokens[1];	
							int theBnum = Integer.parseInt(BName);
							// �z��ɍ��킹�Đ����𕪉�
							int j = theBnum % 8;
							int i = theBnum / 8;
							// �F�̐��l�擾
							int theColor = Integer.parseInt(inputTokens[2]);
							if (theColor == myColor) {
								buttonArray[j][i].setIcon(myIcon);
							} else {
								buttonArray[j][i].setIcon(yourIcon);
							}
							// �{�^�����N���b�N�ł�����^�[���I��
							myTurn = 1 - myTurn;

							// �R�}��u���ꏊ���Ȃ��Ȃ������̏���
							fin--;	
                            if(fin == 0){
								System.out.println(judge());
								break;
                            }
						}
						
						// �Ђ�����Ԃ����R�}������ꍇ(FLIP)
						if (cmd.equals("FLIP")) {
							String theBName = inputTokens[1];
							int theBnum = Integer.parseInt(theBName);
							int j = theBnum % 8;
							int i = theBnum / 8;
							int theColor = Integer.parseInt(inputTokens[2]);
							if (theColor == myColor) {
								buttonArray[j][i].setIcon(myIcon);
							} else {
								buttonArray[j][i].setIcon(yourIcon);
							}
						}
						
						// �p�X�Ȃ�
						if (pass()) {
							contPass++;
							myTurn = 1 - myTurn;
						} else {
							contPass = 0;
						}
						if (contPass == 2) {
							System.out.println(judge());
							break;
						}
						
					} else {
						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("�G���[���������܂���: " + e);
			}
		}
	}
	
	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
			
	}
	
  	// �{�^�����N���b�N�����Ƃ��̏���
	public void mouseClicked (MouseEvent e) {
		if (((myTurn == 0) && (myColor == 1) )||((myTurn == 1) && (myColor == 0))) {
			JButton theButton = (JButton)e.getComponent();			
			String theArrayIndex = theButton.getActionCommand();	//�@�{�^���̔ԍ��擾
			Icon theIcon = theButton.getIcon();						//�@theIcon�ɂ͂ǂ̃A�C�R�����N���b�N���������i�[

			// �N���b�N�����ꏊ���{�[�h�̃A�C�R���̎�
			if (theIcon == boardIcon) {
				int temp = Integer.parseInt(theArrayIndex);
				int x = temp / 8;
				int y = temp % 8;
				repaint();	// ��ʂ̃I�u�W�F�N�g��`�悵����
				
				// judgeButton�Œu���邩�ǂ����̔���
				if (judgeButton(y, x)) {
					//�@�T�[�o�ɏ��𑗂�
					String msg = "PLACE" + " " + theArrayIndex + " " + myColor;
					out.println(msg);	//�@���M�f�[�^���o�b�t�@�ɏ����o��
					out.flush();		//�@���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
				} else {
					// �u���Ȃ��ꍇ
					System.out.println("�����ɂ͔z�u�ł��܂���");
				}
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) { // �}�E�X���I�u�W�F�N�g�ɓ������Ƃ��̏���
	}
	
	public void mouseExited(MouseEvent e) { // �}�E�X���I�u�W�F�N�g����o���Ƃ��̏���
	}
	
	public void mousePressed(MouseEvent e) { // �}�E�X�ŃI�u�W�F�N�g���������Ƃ��̏����i�N���b�N�Ƃ̈Ⴂ�ɒ��Ӂj
	}

	public void mouseReleased(MouseEvent e) { // �}�E�X�ŉ����Ă����I�u�W�F�N�g�𗣂����Ƃ��̏���
	}

	public void mouseDragged(MouseEvent e) { // �}�E�X�ŃI�u�W�F�N�g�Ƃ��h���b�O���Ă���Ƃ��̏���
	}

	public void mouseMoved(MouseEvent e) { // �}�E�X���I�u�W�F�N�g��ňړ������Ƃ��̏���	
	}
	
	// �N���b�N�����ꏊ�ɃR�}��u���邩�ǂ����̏���
	public boolean judgeButton (int y, int x) {
		boolean flag = false;
		// �R�}��u���������8�������ׂĂ��`�F�b�N
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				// �u���ꂽ�ꏊ�͖���
				if ((j == 0) && (i == 0)) {
					continue;
				}
				// flipButtons�Ŋe�����̃R�}�������Ђ�����Ԃ邩
				int flipnum = flipButtons(y, x, j, i);
				if (flipnum >= 1) {
					// �P�ȏ�q�b�N���Ԃ����PLACE������
					flag = true;
					// �Ђ�����Ԃ����R�}�����̏���
					for (int dy = j, dx = i, k = 0; k < flipnum; k++, dy += j, dx += i) {
						//�{�^���̈ʒu�������
						int msgy = y + dy;
						int msgx = x + dx;
						int theArrayIndex = msgx * 8 + msgy;
  
						//�T�[�o�ɏ��𑗂�
						String msg = "FLIP" + " " + theArrayIndex + " " + myColor;
						out.println(msg);
						out.flush();
					}
				}
			}
		}
		return flag;
	}
	
	// �e�����̃R�}�������Ђ�����Ԃ邩��Ԃ�
	public int flipButtons (int y, int x, int j, int i) {
		int flipNum = 0;
		for (int dy = j, dx = i; ; dy += j, dx += i) {
			if ((y+dy < 0) || (y+dy > 7) || (x+dx < 0) || (x+dx > 7)) {
				return 0;
			}
			Icon theIcon = buttonArray[y+dy][x+dx].getIcon();
			// �ǂ̃R�}�ɓ����邩
			if (theIcon == boardIcon) {
				// ���ׂ�����̃R�}������������
				return 0;
			} else if (theIcon == yourIcon) {
				// �G�A�C�R���̎�
				flipNum = flipNum + 1;
			} else {
				// ���R�}�̐F�ɂ���������q�b�N���ς��鐔��Ԃ��B
				return flipNum;
			}
		}
	}

	// �p�X
	public boolean pass() {
		boolean flag = true;
		for (int r = 0; r < 8 ; r++) {
			for (int l = 0; l < 8 ; l++){
				// �R�}��u���������8�������ׂĂ��`�F�b�N
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						// �u���ꂽ�ꏊ�͖���
						if ((j == 0) && (i == 0)) {
							continue;
						}
						// flipButtons�Ŋe�����̃R�}�������Ђ�����Ԃ邩
						int flipnum = flipButtons(l, r, j, i);
						if (flipnum >= 1) {
							flag = false;
							return flag;
						}
					}
				}	
			}
		}
		return flag;
	}


	// ���s����
	public String judge() {
		String text = "";
		int mine = 0;
		int yours = 0;
		for (int r = 0; r < 8 ; r++) {
			for (int l = 0; l < 8 ; l++){
				Icon theIcon = buttonArray[r][l].getIcon();
				if (theIcon == myIcon) {
					mine++;
				} else if (theIcon == yourIcon) {
					yours++;
				}
			}
		}

		if (mine < yours) {
			dlg.win();
			text = "���Ȃ��̏����ł�.";
		
		} else if (mine > yours) {
			dlg.lose();
			text = "����̏����ł�.";

		} else {
			dlg.draw();
			text = "���������ł�.";

		}
		return text;
	}

}