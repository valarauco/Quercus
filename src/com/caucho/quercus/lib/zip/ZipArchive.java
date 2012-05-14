/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Manuel Delgado <manuel.delgado@ucr.ac.cr>
 */
package com.caucho.quercus.lib.zip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.caucho.quercus.annotation.NotNull;
import com.caucho.quercus.annotation.Optional;
import com.caucho.quercus.annotation.ReturnNullAsFalse;
import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.UnicodeValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.lib.file.FileInput;
import com.caucho.quercus.lib.file.FileOutput;
import com.caucho.util.L10N;
import com.caucho.vfs.FilePath;
import com.caucho.vfs.Path;

/**
 * PHP ZipArchive class
 */
public class ZipArchive {

   private static final Logger log = Logger.getLogger(ZipArchive.class.getName());
   private static final L10N L = new L10N(ZipArchive.class);
   
   public static final int CREATE = 1;
   public static final int EXCL = 2;
   public static final int CHECKCONS = 4;
   public static final int OVERWRITE = 8;
   public static final int FL_NOCASE = 1;
   public static final int FL_NODIR = 2;
   public static final int FL_COMPRESSED = 4;
   public static final int FL_UNCHANGED = 8;
   public static final int FL_RECOMPRESS = 16;
   public static final int CM_DEFAULT = -1;
   public static final int CM_STORE = 0;
   public static final int CM_SHRINK = 1;
   public static final int CM_REDUCE_1 = 2;
   public static final int CM_REDUCE_2 = 3;
   public static final int CM_REDUCE_3 = 4;
   public static final int CM_REDUCE_4 = 5;
   public static final int CM_IMPLODE = 6;
   public static final int CM_DEFLATE = 8;
   public static final int CM_DEFLATE64 = 9;
   public static final int CM_PKWARE_IMPLODE = 10;
   public static final int CM_BZIP2 = 12;
   public static final int ER_OK = 0;
   public static final int ER_MULTIDISK = 1;
   public static final int ER_RENAME = 2;
   public static final int ER_CLOSE = 3;
   public static final int ER_SEEK = 4;
   public static final int ER_READ = 5;
   public static final int ER_WRITE = 6;
   public static final int ER_CRC = 7;
   public static final int ER_ZIPCLOSED = 8;
   public static final int ER_NOENT = 9;
   public static final int ER_EXISTS = 10;
   public static final int ER_OPEN = 11;
   public static final int ER_TMPOPEN = 12;
   public static final int ER_ZLIB = 13;
   public static final int ER_MEMORY = 14;
   public static final int ER_CHANGED = 15;
   public static final int ER_COMPNOTSUPP = 16;
   public static final int ER_EOF = 17;
   public static final int ER_INVAL = 18;
   public static final int ER_NOZIP = 19;
   public static final int ER_INTERNAL = 20;
   public static final int ER_INCONS = 21;
   public static final int ER_REMOVE = 22;
   public static final int ER_DELETED = 23;
   
   private FilePath _path;

   
   /**
    * Add a new directory
    * @param dirname The directory to add.
    */
   public Value addEmptyDir(Env env, @NotNull String dirname) {
	   if ( this._path != null && dirname != null ) {
		   try {
			   ZipOutputStream outArchive = new ZipOutputStream(new FileOutput(env, this._path));
			   
			   if ( !dirname.endsWith("/") ){
				   dirname = dirname + "/";
			   }
			   ZipEntry entry = new ZipEntry(dirname);
			   outArchive.putNextEntry(entry);
			   outArchive.flush();
			   outArchive.close();
			   return BooleanValue.TRUE;

		   } catch (IOException e) {
			   env.warning(L.l(e.toString()));
			   log.log(Level.FINE, e.toString(), e);
			   
		   }
	   } 
	   return BooleanValue.FALSE;
   }
   
   /**
    * Adds a file to a ZIP archive from the given path
    * @param filename The path to the file to add.
    * @param localname If supplied, this is the local name inside the ZIP archive that will override the filename.
    * @param start This parameter is not used but is required to extend ZipArchive.
    * @param length This parameter is not used but is required to extend ZipArchive.
    */
   public Value addFile(Env env, @NotNull String filename,
		   @Optional String localname,
		   @Optional("0") int start,
		   @Optional("0") int length) {
	   
	   if ( this._path != null && filename != null ) {
		   try {
			   ZipOutputStream outArchive = new ZipOutputStream(new FileOutput(env, this._path));
			   
			   if (localname == null || localname.isEmpty()) {
				   localname = filename;
			   }

		   
			   FilePath path = new FilePath(env.getRealPath(filename));
			   FileInput file = new FileInput(env, path);
			   outArchive.putNextEntry(new ZipEntry(localname));

			   int len;
			   byte[] buf = new byte[1024];
			   while ((len = file.read(buf)) > 0) {
				   outArchive.write(buf, 0, len);
			   }

			   outArchive.flush();
			   outArchive.closeEntry();
			   file.close();
			   outArchive.close();
			   return BooleanValue.TRUE;

		   } catch (IOException e) {
			   env.warning(L.l(e.toString()));
			   log.log(Level.FINE, e.toString(), e);
		   }
	   } 	   
	   return BooleanValue.FALSE;
   }
   
   /**
    * TODO: Not Implemented yet
    * Add a file to a ZIP archive using its contents
    * @param localname The name of the entry to create.
    * @param contents The contents to use to create the entry. It is used in a binary safe mode.
    */
   public Value addFromString(@NotNull String localname,
		   @NotNull String contents) {
	   return BooleanValue.FALSE;
   }
   
   /** 
    * Close the active archive (opened or newly created)
    */
   public Value close(Env env) {
	   if (this._path != null)
		   this._path = null;
	   return BooleanValue.TRUE;
   }
   
   /**
    * TODO: Not Implemented yet
    * delete an entry in the archive using its index
    * @param index Index of the entry to delete. 
    */
   public Value deleteIndex(@NotNull int index) {
	   return BooleanValue.FALSE;
   }
   
   /**
    * TODO: Not Implemented yet
    * Delete an entry in the archive using its name
    * @param name Name of the entry to delete.
    */
   public Value deleteName(@NotNull String name) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: parameter 'entries' not supported yet
    * Extract the archive contents
    * @param destination Location where to extract the files.
    * @param entries The entries to extract. It accepts either a single entry name or an array of names.
    */
   public Value extractTo(Env env, @NotNull String destination,
		   @Optional String[] entries) {
	   
	   if ( this._path != null && destination != null && !destination.isEmpty() ) {
		   try {
			   ZipInputStream inArchive = new ZipInputStream(new FileInput(env, this._path));
			   
			   Path path = new FilePath(env.getRealPath(destination));
	
			   ZipEntry entry;
			   while ( (entry = inArchive.getNextEntry()) != null){

				   FilePath file = new FilePath(path.getPath() + "/" + entry.getName());

				   file.getParent().mkdirs();
				   file.createNewFile();

				   if ( !entry.isDirectory() ){
					   FileOutput outFile = new FileOutput(env, file);
					   byte[] buf = new byte[1024];
					   int len;
					   while ((len = inArchive.read(buf)) > 0) {
						   outFile.write(buf, 0, len);
					   }
					   outFile.close();
				   }
			   }
			   return BooleanValue.TRUE;
		   } catch (IOException e) {
			   env.warning(L.l(e.toString()));
			   log.log(Level.FINE, e.toString(), e);
		   }

	   }	   
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the Zip archive comment
    * @param flags If flags is set to ZIPARCHIVE::FL_UNCHANGED, the original unchanged comment is returned.
    */
   public Value getArchiveComment(@Optional int flags) {
	   return UnicodeValue.EMPTY;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the comment of an entry using the entry index
    * @param index Index of the entry
    * @param flags If flags is set to ZIPARCHIVE::FL_UNCHANGED, the original unchanged comment is returned.
    */
   public Value getCommentIndex(@NotNull int index,
		   @Optional int flags) {
	   return UnicodeValue.EMPTY;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the comment of an entry using the entry name
    * @param name Name of the entry 
    * @param flags If flags is set to ZIPARCHIVE::FL_UNCHANGED, the original unchanged comment is returned.
    */
   @ReturnNullAsFalse
   public Value getCommentName(@NotNull String name,
		   @Optional int flags) {
	   return UnicodeValue.EMPTY;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the entry contents using its index
    * @param index Index of the entry
    * @param length The length to be read from the entry. If 0, then the entire entry is read.
    * @param flags The flags to use to open the archive. the following values may be ORed to it.
    *         ZIPARCHIVE::FL_UNCHANGED
    *         ZIPARCHIVE::FL_COMPRESSED
    */
   public Value getFromIndex(@NotNull int index,
		   @Optional int length, 
		   @Optional int flags) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the entry contents using its name
    * @param name Name of the entry 
    * @param length The length to be read from the entry. If 0, then the entire entry is read.
    * @param flags The flags to use to open the archive. the following values may be ORed to it.
    *         ZIPARCHIVE::FL_UNCHANGED
    *         ZIPARCHIVE::FL_COMPRESSED
    */
   public Value getFromName(@NotNull String name,
		   @Optional int length, 
		   @Optional int flags) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the name of an entry using its index
    * @param index Index of the entry.
    * @param flags If flags is set to ZIPARCHIVE::FL_UNCHANGED, the original unchanged name is returned.
    */
   public Value getNameIndex(@NotNull String name,
		   @Optional int flags) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the status error message, system and/or zip messages
    */
   public Value getStatusString() {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Get a file handler to the entry defined by its name (read only).
    * @param name The name of the entry to use.
    */
   public Value getStream(@NotNull String name) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Returns the index of the entry in the archive
    * @param name The name of the entry to look up
    * @param flags The flags are specified by ORing the following values, or 0 for none of them.
    *         ZIPARCHIVE::FL_NOCASE
    *         ZIPARCHIVE::FL_NODIR
    */
   public Value locateName(@NotNull String name,
		   @Optional int flags ) {
	   return BooleanValue.FALSE;
   }

   /**
    * Open a ZIP file archive
    * @param filename The file name of the ZIP archive to open.
    * @param flags The mode to use to open the archive.
    *         ZIPARCHIVE::OVERWRITE
    *         ZIPARCHIVE::CREATE
    *         ZIPARCHIVE::EXCL
    *         ZIPARCHIVE::CHECKCONS
    *         
    *         Returns TRUE on success or the error code.
    */
   public Value open(Env env, @NotNull String filename,
		   @Optional int flags) {
	   
	   try {
		   close(env);
		   FilePath path = new FilePath(env.getRealPath(filename));
		   if ( (flags & ZipArchive.CREATE) == ZipArchive.CREATE ) {
			   if( path.exists() 
					   && ((flags & ZipArchive.OVERWRITE) != ZipArchive.OVERWRITE) ) {
				   return LongValue.create(ZipArchive.ER_EXISTS);
			   } else {
				   if ((flags & ZipArchive.OVERWRITE) == ZipArchive.OVERWRITE){
					   path.remove();
				   }
				   if ( !path.createNewFile() ) {
					   return LongValue.create(ZipArchive.ER_OPEN);
				   }
			   } 
		   }
		   this._path = path;
		   return BooleanValue.TRUE;
		   
	   } catch (ZipException e) {
		   return LongValue.create(ZipArchive.ER_NOZIP);
	   }  catch (FileNotFoundException e) {
		   return LongValue.create(ZipArchive.ER_NOENT);
	   } catch (IOException e) {
		   return LongValue.create(ZipArchive.ER_OPEN);
	   }
   }

   /**
    * TODO: Not Implemented yet
    * Renames an entry defined by its index
    * @param index Index of the entry to rename.
    * @param newname New name.
    */
   public Value renameIndex(@NotNull int index, 
		   @NotNull String newname) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Renames an entry defined by its name
    * @param name Name of the entry to rename.
    * @param newname New name.
    */
   public Value renameName(@NotNull String name, 
		   @NotNull String newname) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Set the comment of a ZIP archive
    * @param comment The contents of the comment.
    */
   public Value setArchiveComment(@NotNull String comment) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Set the comment of an entry defined by its index
    * @param index Index of the entry.
    * @param comment The contents of the comment.
    */
   public Value setCommentIndex(@NotNull int index,
		   @NotNull String comment) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Set the comment of an entry defined by its name
    * @param name Name of the entry.
    * @param comment The contents of the comment.
    */
   public Value setCommentName(@NotNull String name,
		   @NotNull String comment) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Get the details of an entry defined by its index.
    * @param index Index of the entry
    * @param flags ZIPARCHIVE::FL_UNCHANGED may be ORed to it to request information about the original file in the archive, ignoring any changes made.
    */
   public Value statIndex(@NotNull int index,
		   @Optional int flags) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Get the details of an entry defined by its name.
    * @param name Name of the entry
    * @param flags ZIPARCHIVE::FL_UNCHANGED may be ORed to it to request information about the original file in the archive, ignoring any changes made.
    */
   public Value statName(@NotNull String name,
		   @Optional int flags) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Undo all changes done in the archive
    */
   public Value unchangeAll() {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Revert all global changes done in the archive.
    */
   public Value unchangeArchive() {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Revert all changes done to an entry at the given index
    * @param index Index of the entry.
    */
   public Value unchangeIndex(@NotNull int index) {
	   return BooleanValue.FALSE;
   }

   /**
    * TODO: Not Implemented yet
    * Revert all changes done to an entry with the given name.
    * @param name Name of the entry.

    */
   public Value unchangeName(@NotNull String name) {
	   return BooleanValue.FALSE;
   }
   
   public String toString()
   {
     return "ZipArchive[]";
   }

}

