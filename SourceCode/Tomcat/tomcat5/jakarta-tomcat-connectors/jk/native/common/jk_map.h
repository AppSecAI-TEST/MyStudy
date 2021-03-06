/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/***************************************************************************
 * Description: Map object header file                                     *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.8 $                                           *
 ***************************************************************************/

#ifndef JK_MAP_H
#define JK_MAP_H


#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */


struct jk_map;
typedef struct jk_map jk_map_t;

int map_alloc(jk_map_t **m);

int map_free(jk_map_t **m);

int map_open(jk_map_t *m);

int map_close(jk_map_t *m);

void *map_get(jk_map_t *m,
              const char *name,
              const void *def);

int map_get_int(jk_map_t *m,
                const char *name,
                int def);

double map_get_double(jk_map_t *m,
                      const char *name,
                      double def);

char *map_get_string(jk_map_t *m,
                     const char *name,
                     const char *def);

char **map_get_string_list(jk_map_t *m,
                           const char *name,
                           unsigned *list_len,
                           const char *def);

int map_put(jk_map_t *m,
            const char *name,
            const void *value,
            void **old);

int map_read_properties(jk_map_t *m,
                        const char *f);

int map_size(jk_map_t *m);

char *map_name_at(jk_map_t *m,
                  int idex);

void *map_value_at(jk_map_t *m,
                   int idex);
  
/**
 *  Replace $(property) in value.
 * 
 */
char *map_replace_properties(const char *value, jk_map_t *m);


#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* JK_MAP_H */
