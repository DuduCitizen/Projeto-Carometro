package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import model.DAO;
import utils.Validador;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Desktop;

public class Carometro extends JFrame {
	DAO dao = new DAO();

	private Connection con;

	private PreparedStatement pst;
	private ResultSet rs;

	private FileInputStream fis;

	private int tamanho;

	private boolean fotoCarregada = false;

	private static final long serialVersionUID = 1L;
	private JLabel lblStatus;
	private JLabel lblData;
	private JLabel lblRE;
	private JLabel lblNome;
	private JTextField textNome;
	private JTextField textRE;
	private JLabel lblFoto;
	private JButton btnEditar;
	private JButton btnReset;
	private JButton btnNewButton;
	private JPanel contentPane;
	private JScrollPane scrollPaneLista;
	private JList<String> listNomes;
	private JButton btnExcluir;
	private JButton btnCarregar;
	private JButton btnAdicionar;
	private JLabel lblLupa;
	private JButton btnSobre;
	private JButton btnPDF;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Carometro frame = new Carometro();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */

	public Carometro() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				status();
				setarData();
			}
		});
		setResizable(false);
		setTitle("Carometro Vinho");
		setIconImage(Toolkit.getDefaultToolkit().getImage(Carometro.class.getResource("/img/duke.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1069, 504);
		contentPane = new JPanel();
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		scrollPaneLista = new JScrollPane();
		scrollPaneLista.setBorder(null);
		scrollPaneLista.setVisible(false);
		scrollPaneLista.setBounds(193, 158, 330, 145);
		contentPane.add(scrollPaneLista);

		listNomes = new JList<String>();
		listNomes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buscarNome();
			}
		});
		scrollPaneLista.setViewportView(listNomes);

		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.desktop);
		panel.setBounds(0, 400, 1053, 65);
		contentPane.add(panel);
		panel.setLayout(null);

		lblStatus = new JLabel("");
		lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
		lblStatus.setBounds(1011, 22, 32, 32);
		panel.add(lblStatus);

		lblData = new JLabel("");
		lblData.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lblData.setForeground(new Color(128, 0, 255));
		lblData.setBounds(29, 11, 276, 32);
		panel.add(lblData);

		lblRE = new JLabel("Registro Estudante");
		lblRE.setForeground(new Color(64, 0, 128));
		lblRE.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblRE.setBounds(53, 74, 130, 41);
		contentPane.add(lblRE);

		textRE = new JTextField();
		textRE.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		textRE.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String caracteres = "0123456789";

				if (!caracteres.contains(e.getKeyChar() + "")) {
					e.consume();
				}
			}
		});
		textRE.setBounds(193, 86, 198, 20);
		contentPane.add(textRE);
		textRE.setColumns(10);

		textRE.setDocument(new Validador(6));

		lblNome = new JLabel("Nome de Estudante");
		lblNome.setForeground(new Color(64, 0, 128));
		lblNome.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblNome.setBounds(53, 126, 130, 41);
		contentPane.add(lblNome);

		textNome = new JTextField();
		textNome.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		textNome.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				listarNomes();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					scrollPaneLista.setVisible(false);

					int confirma = JOptionPane.showConfirmDialog(null,
							"Estudante não cadastrado. \nDeseja cadastrar este estudante?", "Aviso",
							JOptionPane.YES_NO_OPTION);

					if (confirma == JOptionPane.YES_NO_OPTION) {
						textRE.setEnabled(false);
						btnNewButton.setEnabled(false);
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
						btnPDF.setEnabled(false);
					} else {
						reset();
					}
				}
			}
		});
		textNome.setColumns(10);
		textNome.setBounds(193, 138, 330, 20);
		contentPane.add(textNome);

		textNome.setDocument(new Validador(30));

		lblFoto = new JLabel("");
		lblFoto.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFoto.setIcon(
				new ImageIcon(Carometro.class.getResource("/img/photo-camera-interface-symbol-for-button (1).png")));
		lblFoto.setBounds(655, 55, 256, 256);
		contentPane.add(lblFoto);

		btnCarregar = new JButton("Carregar Foto");
		btnCarregar.setEnabled(false);
		btnCarregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carregarFoto();
			}
		});
		btnCarregar.setForeground(SystemColor.window);
		btnCarregar.setBackground(SystemColor.desktop);
		btnCarregar.setBounds(715, 345, 150, 23);
		contentPane.add(btnCarregar);

		btnEditar = new JButton("");
		btnEditar.setEnabled(false);
		btnEditar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editar();
			}
		});
		btnEditar.setIcon(new ImageIcon(Carometro.class.getResource("/img/edit.png")));
		btnEditar.setToolTipText("Editar");
		btnEditar.setBounds(203, 325, 64, 64);
		contentPane.add(btnEditar);

		btnReset = new JButton("");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		btnReset.setToolTipText("Apagar");
		btnReset.setIcon(new ImageIcon(Carometro.class.getResource("/img/sweeping.png")));
		btnReset.setBounds(127, 325, 64, 64);
		contentPane.add(btnReset);

		btnNewButton = new JButton("Buscar");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buscarRE();
			}
		});
		btnNewButton.setBackground(new Color(0, 0, 0));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setBounds(53, 195, 127, 23);
		contentPane.add(btnNewButton);

		btnAdicionar = new JButton("");
		btnAdicionar.setEnabled(false);
		btnAdicionar.setToolTipText("Adicionar");
		btnAdicionar.setIcon(new ImageIcon(Carometro.class.getResource("/img/plus.png")));
		btnAdicionar.setBounds(53, 325, 64, 64);
		contentPane.add(btnAdicionar);

		btnExcluir = new JButton("");
		btnExcluir.setEnabled(false);
		btnExcluir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				excluir();
			}
		});
		btnExcluir.setToolTipText("Excluir");
		btnExcluir.setIcon(new ImageIcon(Carometro.class.getResource("/img/lixo.png")));
		btnExcluir.setBounds(277, 325, 64, 64);
		contentPane.add(btnExcluir);

		lblLupa = new JLabel("");
		lblLupa.setIcon(new ImageIcon(Carometro.class.getResource("/img/lupa.png")));
		lblLupa.setBounds(560, 141, 24, 24);
		contentPane.add(lblLupa);

		btnSobre = new JButton("");
		btnSobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Sobre sobre = new Sobre();
				sobre.setVisible(true);
			}
		});
		btnSobre.setContentAreaFilled(false);
		btnSobre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSobre.setBorderPainted(false);
		btnSobre.setIcon(new ImageIcon(Carometro.class.getResource("/img/info.png")));
		btnSobre.setBounds(53, 242, 48, 48);
		contentPane.add(btnSobre);

		btnPDF = new JButton("");
		btnPDF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gerarPDF();
			}
		});
		btnPDF.setToolTipText("Gerar Lista de Estudantes");
		btnPDF.setIcon(new ImageIcon(Carometro.class.getResource("/img/pdf.png")));
		btnPDF.setBounds(351, 325, 64, 64);
		contentPane.add(btnPDF);

		this.setLocationRelativeTo(null);

	}

	private void status() {
		try {
			con = dao.conectar();

			if (con == null) {
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
			} else {
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
			}

			con.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void setarData() {
		Date data = new Date();
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);

		lblData.setText(formatador.format(data));
	}

	private void carregarFoto() {
		JFileChooser jfc = new JFileChooser();

		jfc.setDialogTitle("Selecionar Arquivos");
		jfc.setFileFilter(
				new FileNameExtensionFilter("Arquivos de imagens(*.PNG, *.JPG, *.JPEG)", "png", "jpg", "jpeg"));

		int resultado = jfc.showOpenDialog(this);

		if (resultado == JFileChooser.APPROVE_OPTION) {
			try {
				fis = new FileInputStream(jfc.getSelectedFile());
				tamanho = (int) jfc.getSelectedFile().length();
				Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblFoto.getWidth(),
						lblFoto.getHeight(), Image.SCALE_SMOOTH);
				lblFoto.setIcon(new ImageIcon(foto));
				lblFoto.updateUI();

				fotoCarregada = true;

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void buscarRE() {
		if (textRE.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digitar o registro do estudante");
			textRE.requestFocus();
		} else {
			String readRE = "select * from estudantes where re = ?";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(readRE);
				pst.setString(1, textRE.getText());

				rs = pst.executeQuery();

				if (rs.next()) {
					textNome.setText(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);

					byte[] img = blob.getBytes(1, (int) blob.length());

					BufferedImage imagem = null;

					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));
					} catch (Exception e) {
					}
					ImageIcon icone = new ImageIcon(imagem);
					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), Image.SCALE_SMOOTH));
					lblFoto.setIcon(foto);

					textRE.setEnabled(false);
					btnNewButton.setEnabled(false);
					btnCarregar.setEnabled(true);
					btnEditar.setEnabled(true);
					btnExcluir.setEnabled(true);
					btnPDF.setEnabled(false);

				} else {
					int confirma = JOptionPane.showConfirmDialog(null,
							"Estudante não cadastrado\nDeseja iniciar um novo cadastro?", "Aviso",
							JOptionPane.YES_NO_OPTION);

					if (confirma == JOptionPane.YES_NO_OPTION) {
						textRE.setEnabled(false);
						textRE.setText(null);
						btnNewButton.setEnabled(false);
						textNome.setText(null);
						textNome.requestFocus();
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
					} else {
						reset();
					}
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void listarNomes() {
		DefaultListModel<String> modelo = new DefaultListModel<>();

		listNomes.setModel(modelo);

		String readLista = "select * from estudantes where nome like '" + textNome.getText() + "%'" + "order by nome";

		try {
			con = dao.conectar();
			pst = con.prepareStatement(readLista);
			rs = pst.executeQuery();

			while (rs.next()) {
				scrollPaneLista.setVisible(true);
				modelo.addElement(rs.getString(2));

				if (textNome.getText().isEmpty()) {
					scrollPaneLista.setVisible(false);
				}

			}

			con.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void buscarNome() {
		int linha = listNomes.getSelectedIndex();

		if (linha >= 0) {
			String readNome = "select * from estudantes where nome like '" + textNome.getText() + "%'"
					+ "order by nome limit " + (linha) + ", 1";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(readNome);
				rs = pst.executeQuery();

				while (rs.next()) {
					scrollPaneLista.setVisible(false);

					textRE.setText(rs.getString(1));
					textNome.setText(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);

					byte[] img = blob.getBytes(1, (int) blob.length());

					BufferedImage imagem = null;

					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));
					} catch (Exception e) {
					}

					ImageIcon icone = new ImageIcon(imagem);
					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), Image.SCALE_SMOOTH));
					lblFoto.setIcon(foto);

					textRE.setEnabled(false);
					btnNewButton.setEnabled(false);
					btnCarregar.setEnabled(true);
					btnEditar.setEnabled(true);
					btnExcluir.setEnabled(true);
					btnPDF.setEnabled(false);
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}

		} else {
			scrollPaneLista.setVisible(false);
		}
	}

	private void editar() {
		if (textNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o nome do estudante");
			textNome.requestFocus();
		} else {

			if (fotoCarregada == true) {
				String update = "update estudantes set nome=?, foto=? where re=?";

				try {
					con = dao.conectar();
					pst = con.prepareStatement(update);
					pst.setString(1, textNome.getText());
					pst.setBlob(2, fis, tamanho);
					pst.setString(3, textRE.getText());

					int confirma = pst.executeUpdate();

					if (confirma == 1) {
						JOptionPane.showMessageDialog(null, "Dados do estudantes alterados!");
					} else {
						JOptionPane.showMessageDialog(null, "Erro! Dados do estudante não alterado!");
					}

				} catch (Exception e) {
					System.out.println(e);
				}
			} else {
				String update = "update estudantes set nome=? where re=?";

				try {
					con = dao.conectar();
					pst = con.prepareStatement(update);
					pst.setString(1, textNome.getText());
					pst.setString(2, textRE.getText());

					int confirma = pst.executeUpdate();

					if (confirma == 1) {
						JOptionPane.showMessageDialog(null, "Dados do estudantes alterados!");
					} else {
						JOptionPane.showMessageDialog(null, "Erro! Dados do estudante não alterado!");
					}

				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	private void excluir() {
		int confirmaExcluir = JOptionPane.showConfirmDialog(null, "Confirma a exlusão desse estudante?", "Atenção!",
				JOptionPane.YES_NO_OPTION);

		if (confirmaExcluir == JOptionPane.YES_NO_OPTION) {
			String delete = "delete from estudantes where re=?";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(delete);
				pst.setString(1, textRE.getText());

				int confirma = pst.executeUpdate();

				if (confirma == 1) {
					reset();
					JOptionPane.showMessageDialog(null, "Estudante exlcuído com sucesso!");
				}

				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	private void gerarPDF() {
		Document document = new Document();

		try {
			PdfWriter.getInstance(document, new FileOutputStream("estudantes.pdf"));
			document.open();
			Date data = new Date();
			DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);
			document.add(new Paragraph("Listagem de estudantes"));
			document.add(new Paragraph(formatador.format(data)));
			document.add(new Paragraph(" "));

			PdfPTable tabela = new PdfPTable(3);
			PdfPCell col1 = new PdfPCell(new Paragraph("RE"));
			tabela.addCell(col1);

			PdfPCell col2 = new PdfPCell(new Paragraph("Nome"));
			tabela.addCell(col2);

			PdfPCell col3 = new PdfPCell(new Paragraph("Foto"));
			tabela.addCell(col3);

			String readLista = "select * from estudantes order by nome";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(readLista);
				rs = pst.executeQuery();

				while (rs.next()) {
					tabela.addCell(rs.getString(1));
					tabela.addCell(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);
					byte[] img = blob.getBytes(1, (int) blob.length());
					com.itextpdf.text.Image imagem = com.itextpdf.text.Image.getInstance(img);

					tabela.addCell(imagem);
				}

				con.close();
			} catch (Exception ex) {
				System.out.println(ex);
			}

			document.add(tabela);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			document.close();

		}
		try {
			Desktop.getDesktop().open(new File("estudantes.pdf"));
		} catch (Exception e2) {
			System.out.println(e2);
		}
	}

	private void reset() {
		scrollPaneLista.setVisible(false);
		textRE.setText(null);
		textNome.setText(null);
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		textNome.requestFocus();

		fotoCarregada = false;
		tamanho = 0;

		textRE.setEnabled(true);
		btnNewButton.setEnabled(true);
		btnCarregar.setEnabled(false);
		btnAdicionar.setEnabled(false);
		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		btnPDF.setEnabled(true);
	}
}
