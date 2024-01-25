/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.transactions;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.uncategorized.transactions.Transactions.Direction;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.Transaction;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.misc.Constants.DECIMAL_FORMAT_98;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TransactionPPTXWriter {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private final List<Transaction> transactions;
	private final int slides;
	private final ZipOutputStream out;

	public TransactionPPTXWriter(List<Transaction> transactions, OutputStream outputStream) {
		this.transactions = transactions;
		this.slides = transactions.size() + 2;
		this.out = new ZipOutputStream(outputStream, UTF_8);
	}

	public void write() throws IOException {
		ZipInputStream in = new ZipInputStream(FileProvider.getData("assets/minecraft/griefer_utils/transactions_export_template"));
		ZipEntry entry;
		while ((entry = in.getNextEntry()) != null) {
			if (entry.isDirectory())
				continue;

			if (entry.getName().contains(".xml")) {
				writeXML(entry.getName(), IOUtils.toString(new InputStreamReader(in, UTF_8)));
				continue;
			}

			out.putNextEntry(new ZipEntry(entry.getName()));
			byte[] buf = new byte[4096];
			int length;
			while ((length = in.read(buf)) != -1)
				out.write(buf, 0, length);
		}

		out.closeEntry();
		out.close();
	}

	private void writeXML(String name, String content) throws IOException {

		switch (name) {
			case "ppt/slides/slide_end.xml":
				out.putNextEntry(new ZipEntry("ppt/slides/slide" + slides + ".xml"));

				BigDecimal received = transactions.stream().filter(t -> Direction.get(t) == Direction.RECEIVED).map(t -> new BigDecimal(t.amount)).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal spent = transactions.stream().filter(t -> Direction.get(t) == Direction.SENT).map(t -> new BigDecimal(t.amount)).reduce(BigDecimal.ZERO, BigDecimal::add);
				received = received.setScale(2, RoundingMode.HALF_EVEN);
				spent = spent.setScale(2, RoundingMode.HALF_EVEN);
				int maxScale = Math.max(received.stripTrailingZeros().scale(), spent.stripTrailingZeros().scale());

				String receivedStr = format(received, maxScale);
				String spentStr = format(spent, maxScale);
				String earned = format(received.subtract(spent), maxScale);
				int maxLen = Math.max(receivedStr.length(), Math.max(spentStr.length(), earned.length()));

				out.write(content
					.replace("{InjectReceived}", leftPad(receivedStr, maxLen) + "$")
					.replace("{InjectSpent}", " " + leftPad(spentStr, maxLen) + "$")
					.replace("{InjectEarned}", "   " + leftPad(earned, maxLen) + "$")
					.getBytes(UTF_8));

				return;

			case "ppt/slides/slide_transaction.xml":
				int i = 0;
				for (Transaction transaction : transactions) {
					out.putNextEntry(new ZipEntry("ppt/slides/slide" + (i++ + 2) + ".xml"));
					out.write(content
						.replace("{InjectId}", String.valueOf(transaction.id))
						.replace("{InjectUser}", transaction.username)
						.replace("{InjectRecipient}", transaction.recipientname)
						.replace("{InjectAmount}", DECIMAL_FORMAT_98.format(transaction.amount))
						.replace("{InjectTime}", dateFormat.format(new Date(transaction.timestamp)))
						.getBytes(UTF_8));
				}
				return;

			case "ppt/slides/_rels/slide_template.xml.rels":
				for (int j = 2; j <= slides; j++) {
					out.putNextEntry(new ZipEntry("ppt/slides/_rels/slide" + j + ".xml.rels"));
					out.write(content.getBytes(UTF_8));
				}
				return;

			case "ppt/_rels/presentation.xml.rels":
				content = content.replace("{InjectPresentationRels}", getRel());
				break;

			case "ppt/presentation.xml":
				content = content
					.replace("{InjectNotesMasterId}", String.valueOf(slides + 2))
					.replace("{InjectSldIdList}", getSldIdList());
				break;

			case "ppt/slides/slide1.xml":
				content = content.replace("{InjectName}", MinecraftUtil.name());
		}

		out.putNextEntry(new ZipEntry(name));
		out.write(content.getBytes(UTF_8));
	}

	private String getRel() {
		StringBuilder sb = new StringBuilder();
		int id = 1;
		for (; id <= slides; ++id)
			sb.append(getRel(id, "slide"));

		for (String value : new String[] {"notesMaster", "presProps", "viewProps", "theme", "tableStyles"})
			sb.append(getRel(id++, value));

		return sb.toString();
	}

	private String getRel(int id, String value) {
		String target = value;
		if (!value.endsWith("Props") && !value.equals("tableStyles"))
			target = String.format("%s%s/%s%d", target, target.equals("theme") ? "" : "s", target, value.equals("slide") ? id : 1);

		return String.format("<Relationship Id=\"rId%d\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/%s\" Target=\"%s.xml\" />", id + 1, value, target);
	}

	private String getSldIdList() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < slides; i++)
			sb.append(String.format("<p:sldId id=\"%d\" r:id=\"rId%d\"/>", 256 + i, i + 2));
		return sb.toString();
	}

	private static String format(BigDecimal value, int maxScale) {
		StringBuilder s = new StringBuilder(DECIMAL_FORMAT_98.format(value.doubleValue()));
		int decimalPlaces = s.length() - s.toString().indexOf(',') - 1;
		int requiredZeroes = maxScale - decimalPlaces;

		if (s.toString().indexOf(',') == -1) {
			requiredZeroes = maxScale;

			if (requiredZeroes != 0)
				s.append(",");
		}

		for (int i = 0; i < requiredZeroes; i++)
			s.append("0");

		return s.toString();
	}

	private static String leftPad(String str, int targetLen) {
		StringBuilder strBuilder = new StringBuilder(str);
		while (strBuilder.length() < targetLen)
			strBuilder.insert(0, " ");
		return strBuilder.toString();
	}

}
