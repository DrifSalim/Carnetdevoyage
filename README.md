Application mobile : Carnet de Voyage, réalisé par : [DRIF Salim / METCHIME Sara]
# Instructions pour tester et compiler ce projet Android

Bonjour,

Merci de prendre en compte les éléments suivants afin de pouvoir compiler, lancer et tester notre application Carnet de Voyage dans les meilleures conditions.  
Nous avons veillé à ce que le dépôt soit complet, cependant, certaines clés ou fichiers sensibles n’y figurent pas pour des raisons de sécurité.

---

## 1. Prérequis

- **Android Studio**
- **Version d’Android ciblée :** Version 12 (API 31)
- **Connexion Internet** (pour le fonctionnement des API externes, Firebase, Google Maps, etc.)

---

## 2. Fichiers et clés à ajouter manuellement

### a) Fichier de configuration Firebase

- **Nom du fichier attendu :** google-services.json
- **Emplacement :** app/
- **Comment l’obtenir :**
    - Se rendre sur la [console Firebase](https://console.firebase.google.com/)
    - Créer ou choississez un projet
    - Télécharger le fichier `google-services.json` correspondant à ce projet
    - Le placer dans le dossier app/ à la racine du module Android

### b) Clé API Google Maps

- **Où la mettre :**
    - Dans le fichier AndroidManifest.xml, à la fin du fichier l’intérieur de la balise <application>, vous trouverez :
      <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="VOTRE_API_KEY_ICI"/>
    - Vous devez generer donc une clé et remplacer "VOTRE_API_KEY_ICI" par la clé obtenue depuis [Google Cloud Platform](https://console.cloud.google.com/apis/credentials)

- **Note importante :**
    - Sans cette clé, la carte Google Maps ne s’affichera pas dans l’application.

---

## 3. Installation et utilisation

1. **Cloner le dépôt**  
   `git clone [URL_DU_DEPOT_GIT]`

2. **Ouvrir le projet dans Android Studio**

3. **Ajouter les fichiers manquants**  
   (cf. section 2 ci-dessus)

4. **Synchroniser Gradle**  
   (Android Studio devrait le proposer automatiquement)

5. **Compiler et lancer l’application**  
   - Sur un émulateur ou appareil physique compatible avec la version Android ciblée

---

## 4. Particularités concernant Firebase

### a) Règles de sécurité Firestore à modifier

Pour permettre le fonctionnement complet de l’application, il est important de modifier les règles de sécurité Firestore et donnez les authorisations de lecture et ecriture
vous pouvez copier l'exemple suivant :

rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2025, 8, 30);
    }
  }
}

- **À faire :**
    - Se rendre dans la console Firebase > Firestore Database > Règles
    - Copier/coller ce bloc de règles pour activer l’accès complet (lecture/écriture) jusqu’au 30 aout 2025.
    - Après cette date, la bdd ne sera plus accessible depuis l'appli, faut donc la mettre à jour si besoin.

---

### b) Indexes Firestore

Il se peut que lors du premier lancement, certaines requêtes Firebase ne fonctionnent pas correctement et provoquent une erreur dans le programme.  
Dans ce cas, il faudra créer manuellement les indexes nécessaires dans Firebase Firestore.

- **Comment faire ?**
    - Lorsqu'une requête nécessite un index manquant, une erreur détaillée s'affichera dans le logcat d’Android Studio.
    - Cette erreur contient un lien direct vers la console Firebase pour créer automatiquement l’index correspondant.
    - Il suffit de cliquer sur le lien proposé, puis de valider la création de l’index dans la console Firebase.

Après la création de l’index, relancez l’application : la requête devrait alors fonctionner normalement.  
Cette manipulation n’est à faire qu'une seule fois par index manquant.

---

### c) Authentification Firebase

Vous devez aussi créer une Authentification Firebase pour que l’application fonctionne correctement.

- **À faire :**
    1. Aller dans la console Firebase > Creer > Authentification
    2. Cliquer sur “Commencer”
    3. Dans “Familiarisez-vous avec Firebase Auth en ajoutant votre première méthode de connexion”, choisir le fournisseur natif **Adresse e-mail/Mot de passe**
    4. **Très important :**  
       - Lors de l’activation, choisir uniquement l’option **Adresse e-mail/Mot de passe**  
       - **Ne pas activer** l’option “Lien envoyé par e-mail (connexion sans mot de passe)”

---

## 5. Équipe

- **Version réalisée :** [Minimaliste / Intermédiaire / Évoluée]
- **Membres du groupe :** [DRIF Salim / METCHIME Sara]
- **Contact :** [salim.drif@etud.u-picardie.fr // sara.metchime@etud.u-picardie.fr]
- Master 1 MIAGE

---

## 6. Remarques

- Si vous rencontrez le moindre souci lors de la compilation ou du lancement de l’application, reportez-vous à la documentation du projet ou contactez-nous aux adresses
  indiquées ci-dessus.
- Nous avons pris soin de ne pas imposer de demandes d’autorisation pour accéder au dépôt et nous avons fait de notre mieux pour tout simplifier pour que vous puissiez
  executer et compiler sans problèmes.

Merci et bonne évaluation !

---
