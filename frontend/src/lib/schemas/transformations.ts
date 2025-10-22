import { z } from 'zod';

export const filterConditionSchema = z.object({
	id: z.string(),
	column: z.string().min(1, 'Column is required'),
	operator: z.enum(['=', '!=', '>', '<', '>=', '<=', 'contains', 'startsWith', 'endsWith', 'regex']),
	value: z.string().min(1, 'Value is required')
});

export const filterConfigSchema = z.object({
	conditions: z.array(filterConditionSchema).min(1, 'At least one condition required'),
	logic: z.enum(['AND', 'OR'])
});

export const sortColumnSchema = z.object({
	id: z.string(),
	column: z.string().min(1, 'Column is required'),
	direction: z.enum(['ASC', 'DESC'])
});

export const sortConfigSchema = z.object({
	columns: z.array(sortColumnSchema).min(1, 'At least one column required')
});

export const joinConfigSchema = z.object({
	joinType: z.enum(['inner', 'left', 'right', 'outer']),
	leftSpreadsheetId: z.number().min(1),
	rightSpreadsheetId: z.number().min(1),
	leftKey: z.string().min(1, 'Left join key is required'),
	rightKey: z.string().min(1, 'Right join key is required'),
	leftPrefix: z.string().optional(),
	rightPrefix: z.string().optional()
});

export const selectConfigSchema = z.object({
	columns: z.array(z.object({
		name: z.string(),
		rename: z.string().optional()
	})).min(1, 'At least one column required')
});

export const renameConfigSchema = z.object({
	mappings: z.record(z.string(), z.string()).refine(
		(mappings) => {
			const values = Object.values(mappings);
			return new Set(values).size === values.length;
		},
		{ message: 'Duplicate column names not allowed' }
	)
});

export const pivotConfigSchema = z.object({
	indexColumns: z.array(z.string()).min(1, 'At least one index column required'),
	valueColumns: z.array(z.string()).min(1, 'At least one value column required'),
	aggregation: z.enum(['sum', 'avg', 'count', 'min', 'max'])
});

export type FilterCondition = z.infer<typeof filterConditionSchema>;
export type FilterConfig = z.infer<typeof filterConfigSchema>;
export type SortColumn = z.infer<typeof sortColumnSchema>;
export type SortConfig = z.infer<typeof sortConfigSchema>;
export type JoinConfig = z.infer<typeof joinConfigSchema>;
export type SelectConfig = z.infer<typeof selectConfigSchema>;
export type RenameConfig = z.infer<typeof renameConfigSchema>;
export type PivotConfig = z.infer<typeof pivotConfigSchema>;
