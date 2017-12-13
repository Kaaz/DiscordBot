/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.command.fun;

import emily.command.CommandVisibility;
import emily.command.CooldownScope;
import emily.command.ICommandCooldown;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.util.DisUtil;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class FightCommand extends AbstractCommand implements ICommandCooldown {
    final private ImageFrame[] frames;
    final private GifPosition[] positions;

    public FightCommand() throws IOException {
        InputStream resource = Launcher.class.getClassLoader().getResourceAsStream("fight/stickman001.gif");
        frames = readGif(resource);
        positions = new GifPosition[frames.length];
        positions[0] = new GifPosition(19, 55, 22, 25);
        positions[1] = new GifPosition(19, 55, 22, 25);
        positions[2] = new GifPosition(19, 55, 22, 25);
        positions[3] = new GifPosition(19, 55, 22, 25);
        positions[4] = new GifPosition(19, 55, 22, 25);
        positions[5] = new GifPosition(19, 55, 22, 25);
        positions[6] = new GifPosition(19, 55, 22, 25);
        positions[7] = new GifPosition(20, 55, 22, 25);
        positions[8] = new GifPosition(20, 60, 22, 25);
        positions[9] = new GifPosition(19, 58, 22, 25);
        positions[10] = new GifPosition(19, 65, 22, 25);
        positions[11] = new GifPosition(29, 55, 22, 25);
        positions[12] = new GifPosition(38, 48, 22, 25);
        positions[13] = new GifPosition(44, 41, 22, 25);
        positions[14] = new GifPosition(60, 36, 22, 25);
        positions[15] = new GifPosition(77, 31, 22, 25);
        positions[16] = new GifPosition(108, 31, 22, 25);
        positions[17] = new GifPosition(105, 28, 22, 25);
        positions[18] = new GifPosition(110, 29, 22, 25);
        positions[19] = new GifPosition(110, 29, 22, 25);
        positions[20] = new GifPosition(109, 29, 22, 25);
        positions[21] = new GifPosition(99, 20, 22, 25);
        positions[22] = new GifPosition(82, 28, 22, 25);
        positions[23] = new GifPosition(38, 53, 22, 25);
        positions[24] = new GifPosition(27, 72, 22, 25);
        positions[25] = new GifPosition(11, 88, 22, 25);
        positions[26] = new GifPosition(56, 85, 22, 25);
        positions[27] = new GifPosition(36, 68, 22, 25);
        positions[28] = new GifPosition(8, 48, 22, 25);
        positions[29] = new GifPosition(6, 60, 22, 25);
        positions[30] = new GifPosition(6, 60, 22, 25);
        positions[31] = new GifPosition(13, 50, 22, 25);
        positions[32] = new GifPosition(25, 43, 22, 25);
        positions[33] = new GifPosition(33, 35, 22, 25);
        positions[34] = new GifPosition(45, 26, 22, 25);
        positions[35] = new GifPosition(45, 26, 22, 25);
        positions[36] = new GifPosition(30, 30, 22, 25);
        positions[37] = new GifPosition(19, 68, 22, 25);
        positions[38] = new GifPosition(19, 68, 22, 25);
        positions[39] = new GifPosition(19, 68, 22, 25);
        positions[40] = new GifPosition(19, 68, 22, 25);
        positions[41] = new GifPosition(19, 68, 22, 25);
        positions[42] = new GifPosition(19, 68, 22, 25);
        positions[43] = new GifPosition(19, 68, 22, 25);
        positions[44] = new GifPosition(19, 68, 22, 25);
        positions[45] = new GifPosition(19, 68, 22, 25);
        positions[46] = new GifPosition(19, 68, 22, 25);
        positions[47] = new GifPosition(19, 68, 22, 25);
        positions[48] = new GifPosition(19, 68, 22, 25);
        positions[49] = new GifPosition(19, 68, 22, 25);
        positions[50] = new GifPosition(19, 68, 22, 25);
        positions[51] = new GifPosition(19, 68, 22, 25);
        positions[52] = new GifPosition(19, 68, 22, 25);
        positions[53] = new GifPosition(19, 68, 22, 25);
        positions[54] = new GifPosition(19, 68, 22, 25);
        positions[55] = new GifPosition(19, 68, 22, 25);
        positions[56] = new GifPosition(19, 68, 22, 25);
        positions[57] = new GifPosition(19, 68, 22, 25);
        positions[58] = new GifPosition(19, 68, 22, 25);
        positions[59] = new GifPosition(19, 68, 22, 25);
        positions[60] = new GifPosition(19, 68, 22, 25);
        positions[61] = new GifPosition(19, 68, 22, 25);
        positions[62] = new GifPosition(19, 68, 22, 25);
        positions[63] = new GifPosition(19, 68, 22, 25);
        positions[64] = new GifPosition(19, 68, 22, 25);
        positions[65] = new GifPosition(15, 68, 22, 25);
        positions[66] = new GifPosition(15, 68, 22, 25);
        positions[67] = new GifPosition(15, 68, 22, 25);
        positions[68] = new GifPosition(15, 68, 22, 25);
        positions[69] = new GifPosition(15, 68, 22, 25);
        positions[70] = new GifPosition(15, 68, 22, 25);
        positions[71] = new GifPosition(15, 68, 22, 25);
        positions[72] = new GifPosition(22, 62, 22, 25);
        positions[73] = new GifPosition(40, 59, 22, 25);
        positions[74] = new GifPosition(51, 66, 22, 25);
        positions[75] = new GifPosition(-1, 68, 22, 25);
        positions[76] = new GifPosition(51, 61, 22, 25);
        positions[77] = new GifPosition(37, 67, 22, 25);
        positions[78] = new GifPosition(28, 73, 22, 25);
        positions[79] = new GifPosition(22, 78, 22, 25);
        positions[80] = new GifPosition(22, 79, 22, 25);
        positions[81] = new GifPosition(35, 77, 22, 25);
        positions[82] = new GifPosition(48, 78, 22, 25);
        positions[83] = new GifPosition(48, 78, 22, 25);
        positions[84] = new GifPosition(45, 75, 22, 25);
        positions[85] = new GifPosition(68, 73, 22, 25);
        positions[86] = new GifPosition(68, 74, 22, 25);
        positions[87] = new GifPosition(68, 72, 22, 25);
        positions[88] = new GifPosition(68, 74, 22, 25);
        positions[89] = new GifPosition(68, 74, 22, 25);
        positions[90] = new GifPosition(68, 74, 22, 25);
        positions[91] = new GifPosition(68, 74, 22, 25);
        positions[92] = new GifPosition(68, 74, 22, 25);
        positions[93] = new GifPosition(68, 74, 22, 25);
        positions[94] = new GifPosition(68, 74, 22, 25);
        positions[95] = new GifPosition(68, 74, 22, 25);
        positions[96] = new GifPosition(68, 74, 22, 25);
        positions[97] = new GifPosition(68, 74, 22, 25);
        positions[98] = new GifPosition(68, 74, 22, 25);
        positions[99] = new GifPosition(68, 74, 22, 25);
        positions[100] = new GifPosition(68, 75, 22, 25);
        positions[101] = new GifPosition(68, 74, 22, 25);
        positions[102] = new GifPosition(68, 74, 22, 25);
        positions[103] = new GifPosition(68, 74, 22, 25);
        positions[104] = new GifPosition(68, 74, 22, 25);
        positions[105] = new GifPosition(68, 74, 22, 25);
        positions[106] = new GifPosition(68, 74, 22, 25);
        positions[107] = new GifPosition(68, 74, 22, 25);
        positions[108] = new GifPosition(68, 74, 22, 25);
        positions[109] = new GifPosition(68, 74, 22, 25);
        positions[110] = new GifPosition(68, 74, 22, 25);
        positions[111] = new GifPosition(68, 74, 22, 25);
        positions[112] = new GifPosition(68, 74, 22, 25);
        positions[113] = new GifPosition(68, 74, 22, 25);
        positions[114] = new GifPosition(62, 78, 22, 25);
        positions[115] = new GifPosition(62, 78, 22, 25);
        positions[116] = new GifPosition(62, 78, 22, 25);
        positions[117] = new GifPosition(62, 78, 22, 25);
        positions[118] = new GifPosition(62, 78, 22, 25);
        positions[119] = new GifPosition(62, 78, 22, 25);
        positions[120] = new GifPosition(14, 100, 22, 25);
        positions[121] = new GifPosition(-6, 153, 22, 25);
        positions[122] = new GifPosition(-6, 153, 22, 25);
        positions[123] = new GifPosition(-6, 153, 22, 25);
        positions[124] = new GifPosition(-6, 153, 22, 25);
        positions[125] = new GifPosition(-6, 153, 22, 25);
        positions[126] = new GifPosition(-6, 153, 22, 25);
        positions[127] = new GifPosition(-6, 153, 22, 25);
        positions[128] = new GifPosition(-6, 153, 22, 25);
        positions[129] = new GifPosition(-6, 153, 22, 25);
        positions[130] = new GifPosition(-6, 153, 22, 25);
        positions[131] = new GifPosition(-6, 153, 22, 25);
        positions[132] = new GifPosition(-6, 153, 22, 25);
    }

    @Override
    public String getDescription() {
        return "get in an epic fight; (gif fight)";
    }

    @Override
    public String getCommand() {
        return "fight";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "fight         //random user fights",
                "fight <user>  //<user> fights",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        TextChannel txt = (TextChannel) channel;
        if (!PermissionUtil.checkPermission(txt, txt.getGuild().getSelfMember(), Permission.MESSAGE_ATTACH_FILES)) {
            return Template.get("permission_missing_attach_files");
        }
        User user = author;
        if (args.length > 0) {
            user = DisUtil.findUser(txt, Misc.joinStrings(args, 0));
        }
        if (user == null) {
            return Template.get("command_user_not_found");
        }
        txt.sendTyping().queue(); //since it might take a while
        try {
            Random rng = new Random();
            BufferedImage avatar = DisUtil.getUserAvatar(user);
            new File("tmp/").mkdirs();
            File f = new File("tmp/fight_" + channel.getId() + ".gif");
            ImageOutputStream output = new FileImageOutputStream(f);
            GifSequenceWriter writer =
                    new GifSequenceWriter(output, frames[0].getImage().getType(), 100, false);
            for (int i = 0; i < frames.length; i++) {
                ImageFrame frame = frames[i];
                GifPosition pos = positions[i];
                Graphics2D g = (Graphics2D) frame.getImage().getGraphics();
                if (pos != null && pos.x != -1) {
                    g.drawImage(avatar, pos.x, pos.y, pos.x + pos.height, pos.y + pos.width, 0, 0, avatar.getWidth(), avatar.getHeight(), null);
                }
                writer.writeToSequence(frame.getImage());
                if (pos == null) {
                    break;
                }
            }
            bot.queue.add(channel.sendFile(f, null), message -> f.delete());
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "";
    }

    @Override
    public long getCooldownDuration() {
        return 15;
    }

    @Override
    public CooldownScope getScope() {
        return CooldownScope.USER;
    }

    public class GifPosition {
        final int x;
        final int y;
        final int height;
        final int width;

        GifPosition(int x, int y, int height, int width) {

            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
        }
    }

    public class ImageFrame {
        private final int delay;
        private final BufferedImage image;
        private final String disposal;
        private final int width, height;

        public ImageFrame(BufferedImage image, int delay, String disposal, int width, int height) {
            this.image = image;
            this.delay = delay;
            this.disposal = disposal;
            this.width = width;
            this.height = height;
        }

        public ImageFrame(BufferedImage image) {
            this.image = image;
            this.delay = -1;
            this.disposal = null;
            this.width = -1;
            this.height = -1;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelay() {
            return delay;
        }

        public String getDisposal() {
            return disposal;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private ImageFrame[] readGif(InputStream in) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>(2);
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(in));

        int lastx = 0;
        int lasty = 0;

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();

        Color backgroundColor = null;

        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
            NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }

            if (globalColorTable != null && globalColorTable.getLength() > 0) {
                IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

                if (colorTable != null) {
                    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                    IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
                    while (colorEntry != null) {
                        if (colorEntry.getAttribute("index").equals(bgIndex)) {
                            int red = Integer.parseInt(colorEntry.getAttribute("red"));
                            int green = Integer.parseInt(colorEntry.getAttribute("green"));
                            int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

                            backgroundColor = new Color(red, green, blue);
                            break;
                        }

                        colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
                    }
                }
            }
        }

        BufferedImage master = null;
        boolean hasBackround = false;

        for (int frameIndex = 0; ; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            NodeList children = root.getChildNodes();

            int delay = Integer.valueOf(gce.getAttribute("delayTime"));

            String disposal = gce.getAttribute("disposalMethod");

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                master.createGraphics().setColor(backgroundColor);
                master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

                hasBackround = image.getWidth() == width && image.getHeight() == height;

                master.createGraphics().drawImage(image, 0, 0, null);
            } else {
                int x = 0;
                int y = 0;

                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);

                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();

                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                if (disposal.equals("restoreToPrevious")) {
                    BufferedImage from = null;
                    for (int i = frameIndex - 1; i >= 0; i--) {
                        if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                            from = frames.get(i).getImage();
                            break;
                        }
                    }

                    {
                        ColorModel model = from.getColorModel();
                        boolean alpha = from.isAlphaPremultiplied();
                        WritableRaster raster = from.copyData(null);
                        master = new BufferedImage(model, raster, alpha, null);
                    }
                } else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null) {
                    if (!hasBackround || frameIndex > 1) {
                        master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
                    }
                }
                master.createGraphics().drawImage(image, x, y, null);

                lastx = x;
                lasty = y;
            }

            {
                BufferedImage copy;

                {
                    ColorModel model = master.getColorModel();
                    boolean alpha = master.isAlphaPremultiplied();
                    WritableRaster raster = master.copyData(null);
                    copy = new BufferedImage(model, raster, alpha, null);
                }
                frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
            }

            master.flush();
        }
        reader.dispose();

        return frames.toArray(new ImageFrame[frames.size()]);
    }

    public class GifSequenceWriter {
        protected ImageWriter gifWriter;
        protected ImageWriteParam imageWriteParam;
        protected IIOMetadata imageMetaData;

        public GifSequenceWriter(
                ImageOutputStream outputStream,
                int imageType,
                int timeBetweenFramesMS,
                boolean loopContinuously) throws IIOException, IOException {
            // my method to create a writer
            gifWriter = getWriter();
            imageWriteParam = gifWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier =
                    ImageTypeSpecifier.createFromBufferedImageType(imageType);

            imageMetaData =
                    gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                            imageWriteParam);

            String metaFormatName = imageMetaData.getNativeMetadataFormatName();

            IIOMetadataNode root = (IIOMetadataNode)
                    imageMetaData.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlExtensionNode = getNode(
                    root,
                    "GraphicControlExtension");

            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute(
                    "transparentColorFlag",
                    "FALSE");
            graphicsControlExtensionNode.setAttribute(
                    "delayTime",
                    Integer.toString(timeBetweenFramesMS / 10));
            graphicsControlExtensionNode.setAttribute(
                    "transparentColorIndex",
                    "0");

            IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
            commentsNode.setAttribute("CommentExtension", "Created by MAH");

            IIOMetadataNode appEntensionsNode = getNode(
                    root,
                    "ApplicationExtensions");

            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");

            int loop = loopContinuously ? 0 : 1;

            child.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF), (byte)
                    ((loop >> 8) & 0xFF)});
            appEntensionsNode.appendChild(child);

            imageMetaData.setFromTree(metaFormatName, root);

            gifWriter.setOutput(outputStream);

            gifWriter.prepareWriteSequence(null);
        }

        public void writeToSequence(RenderedImage img) throws IOException {
            gifWriter.writeToSequence(
                    new IIOImage(
                            img,
                            null,
                            imageMetaData),
                    imageWriteParam);
        }

        private ImageWriter getWriter() throws IIOException {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
            if (!iter.hasNext()) {
                throw new IIOException("No GIF Image Writers Exist");
            } else {
                return iter.next();
            }
        }
    }

    private static IIOMetadataNode getNode(
            IIOMetadataNode rootNode,
            String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
                    == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }
}
