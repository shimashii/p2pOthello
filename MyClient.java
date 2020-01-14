import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.image.*;	//画像処理に必要
import java.awt.geom.*;		//画像処理に必要
import java.awt.Dialog.*;

public class MyClient extends JFrame implements MouseListener, MouseMotionListener {
	// オセロ競技ボード作成
	private JButton buttonArray[][];

	// 自分のコマの色
	private int myColor;
	// 自分のターンかどうか
	private int myTurn;
	// アイコンが自分のか敵のか	
	private ImageIcon myIcon, yourIcon;
	// コンテナを作成
	private Container c;
	// アイコンを設定
	private ImageIcon blackIcon, whiteIcon, boardIcon;
	// 出力用のライター
	PrintWriter out;
	// boardIconの数を格納
	private int fin = 60;

	// 勝敗判定ダイアログ
	WinDialogWindow dlg = new WinDialogWindow(this);



	public MyClient() {
		// 名前の入力ダイアログを開く
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if (myName.equals("")) {
			// 名前がないときは"No name"
			myName = "No name";
		}

		//ウィンドウを作成する
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//　ウィンドウを閉じるときに，正しく閉じるように設定する
		this.setTitle("MyClient");								//　ウィンドウのタイトルを設定する
		this.setSize(600,500);									//　ウィンドウのサイズを設定する
		c = getContentPane();									//　フレームのペインを取得する

		//アイコンの設定
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");
		
		//　自動レイアウトの設定を行わない
		c.setLayout(null);
		buttonArray = new JButton[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				buttonArray[j][i] = new JButton(boardIcon);	//　ボタンにアイコンを設定する
				c.add(buttonArray[j][i]);					//　ペインに貼り付ける
				buttonArray[j][i].setBounds(i*50+10,j*50+10,50,50);	//　ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）i*45,10,45,45
				buttonArray[j][i].addMouseListener(this);			//　ボタンをマウスでさわったときに反応するようにする
				buttonArray[j][i].addMouseMotionListener(this);		//　ボタンをマウスで動かそうとしたときに反応するようにする
				buttonArray[j][i].setActionCommand(Integer.toString(i*8+j));	//　ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
			}
		}
		// オセロの4コマセット
		buttonArray[3][3].setIcon(whiteIcon);
		buttonArray[4][4].setIcon(whiteIcon);
		buttonArray[3][4].setIcon(blackIcon);
		buttonArray[4][3].setIcon(blackIcon);
		
		//サーバに接続する
		Socket socket = null;
		try {
			// "localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			// 10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			 System.err.println("エラーが発生しました: " + e);
		}
		MesgRecvThread mrt = new MesgRecvThread(socket, myName);	// 受信用のスレッドを作成する
		mrt.start();												// スレッドを動かす（Runが動く）
	}

	// 勝敗のダイアログ(負けと引き分けの画像は用意できていない)
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
		
	//メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {
		Socket socket;
		String myName;
		
		public MesgRecvThread(Socket s, String n){
			socket = s;
			myName = n;
		}

		// 通信状況を監視し，受信データによって動作する
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);	// 接続の最初に名前を送る
				myTurn = 0;				// ターンを設定
				String myNumberStr = br.readLine();
				int myNumberInt = Integer.parseInt(myNumberStr);
				int contPass = 0;
				// 先行後攻とコマの色設定
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
					String inputLine = br.readLine();	// データを一行分だけ読み込んでみる
					if (inputLine != null) {			// 読み込んだときにデータが読み込まれたかどうかをチェックする
						String[] inputTokens = inputLine.split(" ");	// 入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];					// コマンドの取り出し．１つ目の要素を取り出す
						
						// クリックした場所に置けた時に実行(PLASE)
						if (cmd.equals("PLACE")) {
							// ボタン番号取得
							String BName = inputTokens[1];	
							int theBnum = Integer.parseInt(BName);
							// 配列に合わせて数字を分解
							int j = theBnum % 8;
							int i = theBnum / 8;
							// 色の数値取得
							int theColor = Integer.parseInt(inputTokens[2]);
							if (theColor == myColor) {
								buttonArray[j][i].setIcon(myIcon);
							} else {
								buttonArray[j][i].setIcon(yourIcon);
							}
							// ボタンをクリックできたらターン終了
							myTurn = 1 - myTurn;

							// コマを置く場所がなくなった時の処理
							fin--;	
                            if(fin == 0){
								System.out.println(judge());
								break;
                            }
						}
						
						// ひっくり返せすコマがある場合(FLIP)
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
						
						// パスなら
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
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}
	
	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
			
	}
	
  	// ボタンをクリックしたときの処理
	public void mouseClicked (MouseEvent e) {
		if (((myTurn == 0) && (myColor == 1) )||((myTurn == 1) && (myColor == 0))) {
			JButton theButton = (JButton)e.getComponent();			
			String theArrayIndex = theButton.getActionCommand();	//　ボタンの番号取得
			Icon theIcon = theButton.getIcon();						//　theIconにはどのアイコンをクリックしたかを格納

			// クリックした場所がボードのアイコンの時
			if (theIcon == boardIcon) {
				int temp = Integer.parseInt(theArrayIndex);
				int x = temp / 8;
				int y = temp % 8;
				repaint();	// 画面のオブジェクトを描画し直す
				
				// judgeButtonで置けるかどうかの判定
				if (judgeButton(y, x)) {
					//　サーバに情報を送る
					String msg = "PLACE" + " " + theArrayIndex + " " + myColor;
					out.println(msg);	//　送信データをバッファに書き出す
					out.flush();		//　送信データをフラッシュ（ネットワーク上にはき出す）する
				} else {
					// 置けない場合
					System.out.println("そこには配置できません");
				}
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) { // マウスがオブジェクトに入ったときの処理
	}
	
	public void mouseExited(MouseEvent e) { // マウスがオブジェクトから出たときの処理
	}
	
	public void mousePressed(MouseEvent e) { // マウスでオブジェクトを押したときの処理（クリックとの違いに注意）
	}

	public void mouseReleased(MouseEvent e) { // マウスで押していたオブジェクトを離したときの処理
	}

	public void mouseDragged(MouseEvent e) { // マウスでオブジェクトとをドラッグしているときの処理
	}

	public void mouseMoved(MouseEvent e) { // マウスがオブジェクト上で移動したときの処理	
	}
	
	// クリックした場所にコマを置けるかどうかの処理
	public boolean judgeButton (int y, int x) {
		boolean flag = false;
		// コマを置いた周りの8方向すべてをチェック
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				// 置かれた場所は無視
				if ((j == 0) && (i == 0)) {
					continue;
				}
				// flipButtonsで各方向のコマがいくつひっくり返るか
				int flipnum = flipButtons(y, x, j, i);
				if (flipnum >= 1) {
					// １つ以上ヒックリ返せればPLACEも走る
					flag = true;
					// ひっくり返されるコマたちの処理
					for (int dy = j, dx = i, k = 0; k < flipnum; k++, dy += j, dx += i) {
						//ボタンの位置情報を作る
						int msgy = y + dy;
						int msgx = x + dx;
						int theArrayIndex = msgx * 8 + msgy;
  
						//サーバに情報を送る
						String msg = "FLIP" + " " + theArrayIndex + " " + myColor;
						out.println(msg);
						out.flush();
					}
				}
			}
		}
		return flag;
	}
	
	// 各方向のコマがいくつひっくり返るかを返す
	public int flipButtons (int y, int x, int j, int i) {
		int flipNum = 0;
		for (int dy = j, dx = i; ; dy += j, dx += i) {
			if ((y+dy < 0) || (y+dy > 7) || (x+dx < 0) || (x+dx > 7)) {
				return 0;
			}
			Icon theIcon = buttonArray[y+dy][x+dx].getIcon();
			// どのコマに当たるか
			if (theIcon == boardIcon) {
				// 調べる方向のコマが何も無い時
				return 0;
			} else if (theIcon == yourIcon) {
				// 敵アイコンの時
				flipNum = flipNum + 1;
			} else {
				// 自コマの色にあたったらヒックリ変える数を返す。
				return flipNum;
			}
		}
	}

	// パス
	public boolean pass() {
		boolean flag = true;
		for (int r = 0; r < 8 ; r++) {
			for (int l = 0; l < 8 ; l++){
				// コマを置いた周りの8方向すべてをチェック
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						// 置かれた場所は無視
						if ((j == 0) && (i == 0)) {
							continue;
						}
						// flipButtonsで各方向のコマがいくつひっくり返るか
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


	// 勝敗判定
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
			text = "あなたの勝ちです.";
		
		} else if (mine > yours) {
			dlg.lose();
			text = "相手の勝ちです.";

		} else {
			dlg.draw();
			text = "引き分けです.";

		}
		return text;
	}

}