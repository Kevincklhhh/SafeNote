# SafeNote
A notebook application that encrypts your notes. The user must create a local account with a username and password(requires password) [Storage].
The application stores the user information safely with crypto API. The user can write a note, adding location to it[Sensors/Sensitive API]).
The application stores the texts and location data as a note object. The location can be redirected to a map app like Google Map[Inter-application].
The application also stores the hash of the note to protect its integrity. 
Everytime the app runs, it can check the hash to tell whether the note has been modified by some other application.

Android Keystore 
