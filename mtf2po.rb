#! /usr/env ruby
=begin
The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2013, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.
=end


def process_file(input)
	while(!input.eof?)
		line = input.gets.strip
		if line.index('#_') == 0
			english = line
			translated = input.gets.strip
			process_entry(english, translated)
		end
	end
end

def process_entry(english, translated)
	filler, english_text = get_stuff_before_and_after_equals(english)
	hash_and_context, translated_text = get_stuff_before_and_after_equals(translated)
	
	hash = hash_and_context[1,4]
	context = hash_and_context[6..-1]
	
	
	puts
	puts "#: #{hash}"
	if(english_text.index("\\n"))
		puts "#. Do NOT translate the \\n because they represent newlines."
	end
	if(english_text.index("Benetech"))
		puts "#. Do NOT translate the word Benetech."
	end
	if(english_text.index("Martus"))
		puts "#. Do NOT translate the word Martus."
	end
	if(english_text.index("Tor"))
		puts "#. Do NOT translate the word Tor."
	end
	if(english_text =~ /#.*#/)
		puts "#. Do not translate words that are surrounded by #'s, but you may move " + 
		"them around as grammatically appropriate. " +
		"Example: #TotalNumberOfFilesInBackup#, #Titles#, #FieldLabel#, etc. " +
		"as these words will be replaced when the program runs with " +
		"a particular value. " +
		"For Example. #TotalNumberOfFilesInBackup# = '5' " +
		"#Titles# = 'A list of bulletin titles' "
	end
	if(english_text =~ /\(\..*\)/)
		puts "#. For file filters like 'Martus Report Format (.mrf), " +
		"The descriptive names should be translated, but the (.mrf) must not be translated."
	end
	if(context == "field:VirtualKeyboardKeys")
		puts "#. Keep the english alphabet, but include any " + 
		"non-english characters at the end of the english alphabet/numbers/special " + 
		"characters (e.g. attach entire Thai alphabet at the end of the line)."
	end
	if(context == "field:translationVersion")
		puts "#. Do not translate the numbers."
	end
	if(context == "field:ErrorCustomFields")
		puts "#. Do not translate the numbers."
	end
	if(context.index("CreateCustomFieldsHelp"))
		puts "#. You can translate tags into foreign characters (but without punctuation or spaces)."
		puts "#. Check the User Guide section 10b to see if the text has already been translated and use the same translation for consistency."
	end
	if(context.index("CreateCustomFieldsHelp1") || context.index("CreateCustomFieldsHelp2"))
		puts "#. Leave standard field tags in English, but put translation in parentheses after " + 
		"english : e.g.  'author' (translation-of-author from mtf, e.g. autor in spanish), " +
		"so users know what they refer to."
	end
	if(context.index("CreateCustomFieldsHelp2"))
		puts "#. Leave field types in English (e.g. BOOLEAN, DATE), " + 
		"but put translation in parentheses after english, so users know what they refer to."
		puts "#. Change the \"ddd\" in \"<DefaultValue>ddd</DefaultValue>\" to whatever letter the translation of \"default\" begins with."
	end
	if(context.index("CreateCustomFieldsHelp3"))
		puts "#. Leave field types in English in examples (e.g. BOOLEAN, DATE)
		puts "#. do not translate words between angle brackets in the XML for custom fields, such as: " +
		"<Field type='SECTION'>, <Field type='STRING'>, <Field type='BOOLEAN'>, <Field type='DATE'>, " + 
		"<Field type='DATERANGE'>, <Field type='DROPDOWN'>, <Field type='MULTILINE'>  " +
		"<Field type='LANGUAGE'>, <Field type='MESSAGE'>, <Field type='GRID'>,  " +
		"</Field>, <Tag>, </Tag>, <Label>, </Label>,  <Message>, </Message>  " +
		"<Choices>, </Choices>, <Choice>, </Choice>, <DataSource>, </DataSource> " +
		"<GridFieldTag>, </GridFieldTag>, <GridColumnLabel>, </GridColumnLabel>  " +
		"<GridSpecDetails>, </GridSpecDetails>, <Column>, </Column>,  " +
		"<Column type='STRING'>, <Column type='BOOLEAN'>, <Column type='DATE'>, " +
		"<Column type='DATERANGE'>, <Column type='DROPDOWN'>  " +
		"<KeepWithPrevious/>, <RequiredField/>, <DefaultValue>, </DefaultValue>, " +
		"<MinimumDate>, </MinimumDate>, <MaximumDate>, </MaximumDate>, <MaximumDate/>. " +
		"For Reusable choices sections, translate anything within single quotes '...', but not  " +
		"<UseReusableChoices code= , </UseReusableChoices> " +
		"<ReusableChoices code= , </ReusableChoices>, label= , <Choice code= ."
	end
	puts "#. #{context}"
	puts "msgid  \"#{english_text}\""
	puts "msgstr \"#{translated_text}\""
end

def get_stuff_before_and_after_equals(text)
	equals = text.index('=')
	before = text[0, equals]
	after = text[equals+1..-1]
	return [before, after]
end

def write_quoted(text)
	puts "  \"#{text}\""
end

def process_header(input)
	while(!input.eof?)
		line = input.gets.strip
		if(line.empty?)
			return
		end
		
		if(line.index("# Language name:") == 0)
			$language_name = extract_after_colon(line)
		elsif (line.index("# Client version") == 0)
			$version = extract_after_colon(line)
		end
	end
	
end

def extract_after_colon(line)
	colon = line.index(':')
	return line[colon+1..-1].strip
end

def write_header
	puts "msgid \"\""
	puts "msgstr \"\""
	write_quoted "Project-Id-Version: Martus #{$version}\\n"
	write_quoted "Report-Msgid-Bugs-To: info@martus.org\\n"
	write_quoted "POT-Creation-Date: #{Time.now}\\n"
	#write_quoted "PO-Revision-Date: \\n"
	#write_quoted "Last-Translator: Jeremy <jeremyy@miradi.org>\\n"
	write_quoted "Language-Team: #{$language_name}\\n"
	write_quoted "MIME-Version: 1.0\\n"
	write_quoted "Content-Type: text/plain; charset=UTF-8\\n"
	write_quoted "Content-Transfer-Encoding: 8bit\\n"
	#write_quoted "Plural-Forms: nplurals=2; plural=(n != 1);\\n"
	#write_quoted "X-Poedit-Bookmarks: 874,-1,-1,-1,-1,-1,-1,-1,-1,-1\\n"
	puts
	puts
end

mtf_filename = "/home/kevins/work/hg/martus/martus/martus-client/source/org/martus/client/swingui/Martus-es.mtf"
File.open(mtf_filename) do | input |
	process_header(input)
	write_header
	process_file(input)
end
 