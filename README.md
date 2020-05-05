# Voikko Finnish Tokenizer for Omegat

## Description

This plugins adds a new `VoikkoTokenizer` in OmegaT. Because it relies on the [Voikko](https://voikko.puimula.org/) library it should gives more accurate tokenization than the default OmegaT tokenizer for language like Finnish.

It also includes Grammar verification and spellchecking. 


## Installation

* You can download the plugin jar file from the [release page](../../releases). The OmegaT plugin jar should be placed in `$HOME/.omegat/plugins` (Linux), `~/Library/Preferences/OmegaT/plugins/` (macOS), or `%APPDATA%\OmegaT\plugins` (Windows) depending on your operating system.

* The Voikko library should also be installed on your system (see [this page](https://voikko.puimula.org/java.html) for detailed instructions).

* The Voikko dictionnaries (which can be [downloaded there](https://www.puimula.org/htp/testing/voikko-snapshot-v5/)) must be placed in the `$HOME/.omegat/voikko/dicts` directory.

Once everything is installed, open your project in OmegaT, select  Project / Properties / Source Language Tokenizer

### Windows library install

* Download the dll corresponding to your to you system (win32 or win64) from this page: https://www.puimula.org/htp/testing/voikko-sdk/win-crossbuild/libvoikko-4.1.1+win1/
   
* Copy the `libvoikko-1.dll` file in the folder `%APPDATA%\OmegaT\voikko`.

## License

This project is distributed under the GNU general public license version 3 or later.

