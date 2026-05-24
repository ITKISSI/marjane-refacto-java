### Consignes: 
* Ignorez les migrations BDD
* Ne pas modifier les classes qui ont un commentaire: `// WARN: Should not be changed during the exercise
`
* Pour lancer les tests (depuis le sous-répertoire `api`) :
  * unitaires: `mvnw test`
  * integration: `mvnw integration-test`
  * tous: `mvnw verify`

---

## Refactoring

### `ProductType` enum
Remplacement des comparaisons de chaînes brutes par un enum typé.

### `OrderService`
Extraction de la logique d'orchestration des commandes hors du contrôleur.

### `ProductService`
Centralisation de toute la logique métier produit avec un point d'entrée unique `processProduct()`, un handler privé par type, et injection par constructeur.

### `MyController`
Réduit à son rôle HTTP uniquement — délègue entièrement à `OrderService`.

### Tests unitaires
Couverture de tous les cas métier pour les types `NORMAL`, `SEASONAL` et `EXPIRABLE`.

---

## Stratégie de branches

Une branche `feature/` par tâche, mergée dans `main` après validation.

```
main
├── feature/product-type-enum
├── feature/order-service
├── feature/product-service-cleanup
└── feature/unit-tests
```
