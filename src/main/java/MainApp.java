import java.awt.EventQueue;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.AWTUtil;
import javax.swing.JLabel;

public class MainApp {

	private JFrame frame;
	List list = new List();
	String[] allowedMedia = { "mp4", "avi" };
	File folder;
	JButton btnGenerateThumbnail;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainApp window = new MainApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 426, 549);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);  
		frame.getContentPane().setLayout(null);

		JButton btnBrowse = new JButton("Browse Video Directory");

		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new java.io.File("."));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);
				int status = fc.showOpenDialog(frame);

				if (status == JFileChooser.APPROVE_OPTION) {
					folder = fc.getSelectedFile();
					list.removeAll();
					if (folder.isDirectory()) {
						File[] files = folder.listFiles();
						for (File f : files) {
							String ext = FilenameUtils.getExtension(f.getAbsolutePath());
							if (Arrays.asList(allowedMedia).contains(ext)) {
								// getImageFromFrame(f);
								list.add(f.getName());
							}
							ext = null;
						}
						files = null;
					}
				}
			}
		});
		btnBrowse.setBounds(16, 49, 391, 29);
		frame.getContentPane().add(btnBrowse);

		JPanel panel = new JPanel();
		panel.setBounds(16, 90, 391, 391);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		list.setBounds(10, 10, 371, 371);
		panel.add(list);

		JLabel lblNewLabel = new JLabel("Allowed Media: mp4, avi");
		lblNewLabel.setBounds(22, 21, 224, 16);
		frame.getContentPane().add(lblNewLabel);

		btnGenerateThumbnail = new JButton("Generate Thumbnail");
		btnGenerateThumbnail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					public void run() {
						btnGenerateThumbnail.setText("Process...");
						btnGenerateThumbnail.setEnabled(false);
						File[] files = folder.listFiles();
						for (File f : files) {
							String ext = FilenameUtils.getExtension(f.getAbsolutePath());
							if (Arrays.asList(allowedMedia).contains(ext)) {
								getImageFromFrame(f);
								// list.add(f.getName());
							}
							ext = null;
						}
						files = null;
						btnGenerateThumbnail.setEnabled(true);
						btnGenerateThumbnail.setText("Generate Thumbnail");
					}
				}).start();

			}

		});
		btnGenerateThumbnail.setBounds(227, 486, 180, 29);
		frame.getContentPane().add(btnGenerateThumbnail);

	}

	private void getImageFromFrame(File videoFile) {
		String fileName = videoFile.getAbsolutePath();
		String baseName = FilenameUtils.getBaseName(videoFile.getAbsolutePath());
		String savePath = FilenameUtils.getFullPath(videoFile.getAbsolutePath());

		System.out.println(fileName);
		double frameNumber = 0d;
		try {
			SeekableByteChannel bc = NIOUtils.readableFileChannel(videoFile);
			MP4Demuxer dm = new MP4Demuxer(bc);
			DemuxerTrack vt = dm.getVideoTrack();
			frameNumber = vt.getMeta().getTotalDuration() / 2.0;
			dm = null;
			vt = null;
			bc = null;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

			Picture frame = FrameGrab.getNativeFrame(new File(fileName), frameNumber);
			BufferedImage img = AWTUtil.toBufferedImage(frame);
			ImageIO.write(img, "png", new File(savePath + "/" + baseName + ".png"));
			fileName = null;
			baseName = null;
			savePath = null;
			img = null;
			frame = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JCodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
